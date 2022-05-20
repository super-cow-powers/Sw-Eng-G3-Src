/*
 * Copyright (c) 2022, Boris Choi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the copyright holder nor the names of its contributors may
 *   be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package g3.project.network;

import g3.project.core.Engine;
import g3.project.core.Threaded;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.event.Event;

/**
 *
 * @author Boris Choi
 */
public final class CommSys extends Threaded {
    private static final int CS_TIMEOUT = 50;

    /**
     * Client Session
     */
    private Client client;
    /**
     * Server Session
     *
     * @param commSys
     */
    private Server server;

    /**
     * Is Connected?
     */
    private AtomicBoolean isConnected = new AtomicBoolean(false);

    /**
     * Is Presenting?
     */
    private AtomicBoolean isPresenting = new AtomicBoolean(false);

    /**
     * Is Paused?
     */
    private AtomicBoolean isPaused = new AtomicBoolean(true);

    /**
     * Server connection action queue.
     */
    private final BlockingQueue<ConnectionInfo> connectionQueue
    = new LinkedBlockingQueue<>();

    /**
     * Client to Server transfer queue.
     */
    private final BlockingQueue<Event> txEventQueue
            = new LinkedBlockingQueue<Event>();

    /**
     * Client to Server buffer queue.
     */
    private final BlockingQueue<Event> txBufferQueue
            = new LinkedBlockingQueue<Event>();

    /**
     * Server to Client buffer queue.
     */
    private final BlockingQueue<Event> rxBufferQueue
            = new LinkedBlockingQueue<Event>(); 

    /**
     * Ref to the Engine
     */
    private final Engine engine;

    /**
     * Constructor.
     * 
     * @param globalEngine Application engine.
     */
    public CommSys(final Engine globalEngine) {
        super();
        this.engine = globalEngine;
    }

    /**
     * Send a connection event to the communication system.
     *
     * @param serverDetails Detail of server to connect to.
     */
    public void offerConnectionEvent(final ConnectionInfo connectionRef) {
        connectionQueue.offer(connectionRef);
        unsuspend();
    }

    /**
     * Send an event to the net if session is presenting.
     *
     * @param event Event to send.
     */
    public void feedEvent(final Event event) {
        if (isPresenting.get()) {
            txEventQueue.offer(event);
            unsuspend();
        }
    }



    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
        //Wait till running properly
        while (!(running.get())) {
        }

        //...
        while (running.get()) {
            //Main thread dispatch loop
            try {
                if (!connectionQueue.isEmpty()) { //New connection event?
                    handleConnection(connectionQueue.take());
                } else if (!txEventQueue.isEmpty()) { //Event to send?
                    transmitEvent(txEventQueue.take());
                } else if (!callQueue.isEmpty()) { //Call to make?
                    callQueue.take().run();
                } else {
                    if (isConnected.get()) {
                        //System.out.println(" CommSys: Waiting for data...");
                        clientCheck(); // Client check for new event recieved
                    } else if (isPresenting.get()) {
                        System.out.println(" CommSys: Check for connection...");
                        serverCheck(); // Server check for new connection
                    } else{
                        System.out.println(" CommSys: Suspending...");
                        suspended.set(true); // Nothing from enngine
                    }
                }

                while (suspended.get()) { // Suspend
                    synchronized (this) {
                        wait();
                    }
                }
            } catch (SocketTimeoutException e) {
                // pause for 50ms
                try {
                    System.out.println(" CommSys: Sleeping...");
                    Thread.sleep(CS_TIMEOUT);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        terminate();
        System.out.println("Comm-System is going down NOW.");
        return;
    }

    /**
     * Handle a connection request.
     *
     * @param connectionRef Connection request.
     */
    private void handleConnection(final ConnectionInfo connectionRef) {
        if (connectionRef.getType().equals("Client")) {
            startViewing(connectionRef);
        } else if (connectionRef.getType().equals("Host")) {
            startHosting(connectionRef);
        } else{
            System.out.println("CommSys: Unknown connection type.");
        }
    }

    /**
     * Begin broadcasting to the server.
     * 
     * @param serverDetails
     */
    private void startHosting(ConnectionInfo connectionRef){
        try{
            server = new Server(connectionRef);
            isConnected.set(false);
            isPresenting.set(true);
            isPaused.set(false);
            System.out.println("Comm-System: Started server.");
        } catch (IOException ex){
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to host - see stack trace", true));
        }
    }

    /**
     * Try viewing a remote session
     * 
     * @param serverDetails
     */
     private void startViewing(ConnectionInfo connectionRef){
        try{
            client = new Client();
            client.connectToServer(connectionRef);
            isConnected.set(true);
            isPresenting.set(false);
            isPaused.set(false);
            System.out.println("Comm-System: Started client.");
        } catch (IOException ex){
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to connect - see stack trace", true));
        }
    }

    /**
     * Stop hosting a session.
     */
    public void stopHosting() {
        if (server != null) {
            server.close();
            server = null;
            isConnected.set(false);
            isPresenting.set(false);
            isPaused.set(true);
            System.out.println("Comm-System: Stopped server.");
        }
    }
    
    /**
     * Stop viewing a session
     */
    private void stopViewing(){
        if (client != null) {
            client.disconnectFromServer();
            client = null;
            isConnected.set(false);
            isPresenting.set(false);
            isPaused.set(true);
            System.out.println("Comm-System: Stopped client.");
        }
    }

    /**
     * Alter the connection status of the server.
     */
    private void sessionUpdate() {
        // Toggle the connection status
        isPaused.set(!isPaused.get());
    }

    /**
     * Upload an event to the server.
     *
     * @param event Event to send.
     * @throws InterruptedException
     */
    private void transmitEvent(final Event event) throws InterruptedException {
        // Try to upload the event to the server
        try {
            txBufferQueue.offer(event);
            if (!isPaused.get()) {
                while(!txBufferQueue.isEmpty()) {
                    server.sendEvent(txBufferQueue.take());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to upload event to server - see stack trace", true));
        }
        
    }

    /*
     @todo - implement the following loops
     make both server and client threaded?
    */

    /**
     * Server check
     * @throws SocketTimeoutException
     */
    private void serverCheck() throws SocketTimeoutException {
        if(isPresenting.get()){
            try {
                // Accept the connection
                server.acceptConnection();
            } catch (SocketTimeoutException ex) {
                throw ex;
            } catch (IOException ex) {
                ex.printStackTrace();
                Platform.runLater(() -> engine.
                        putMessage("Fail to accept connection - see stack trace", true));
            }
        }
    }

    /**
     * Client check
     * @throws InterruptedException
     * @throws SocketTimeoutException
     */
    private void clientCheck() throws InterruptedException, SocketTimeoutException {
        if(isConnected.get()){
            try {
                // Receive the event
                if(client.rxAvailable()){
                    var event = client.readStream();
                    event.ifPresent(e -> {
                        rxBufferQueue.offer((Event) e);
                        System.out.println("CommSys: Received event: " + event);
                    });
                }
                if(!isPaused.get()){
                    // Update local session
                    while(!rxBufferQueue.isEmpty()){
                        Event event = rxBufferQueue.take();
                        //engine.offerEvent(event);
                    }
                }
            } catch (SocketTimeoutException ste) {
                throw ste;
            } catch (SocketException se) {
                System.out.println("CommSys: connection lost.");
                stopViewing();
            } catch (IOException ex) {
                ex.printStackTrace();
                Platform.runLater(() -> engine.
                        putMessage("Fail to receive event from server - see stack trace", true));
            }
        }
    }

    /**
     * Terminate commSys cleanly.
     */
    public void terminate() {
        try {
            if (server != null) {
                server.close();
            }
            stopViewing();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
