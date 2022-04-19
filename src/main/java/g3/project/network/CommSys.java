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
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
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
     * Send an event to the net.
     *
     * @param event Event to send.
     */
    public void sendEvent(final Event event) {
        txEventQueue.offer(event);
        unsuspend();
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
                if (!connectionQueue.isEmpty()) { //New connection request?
                    connectToRemote(connectionQueue.take());
                } else if (!txEventQueue.isEmpty()) { //Event to send?
                    transmitEvent(txEventQueue.take());
                } else {
                    suspended.set(true); // Nothing from enngine
                }

                //@todo Implement checking loop properly
                while (suspended.get()) { // Suspend
                    if (isConnected.get()) { // if connected
                        clientLoop(); // Client loop
                    } else if (isPresenting.get()) { // if presenting
                        serverLoop(); // Server loop
                    } else{
                        synchronized (this) {
                            wait();
                        }
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("Comm-System is going down NOW.");
        return;
    }

    /**
     * Connect to the server.
     *
     * @param serverDetails Remote server details.
     */
    private void connectToRemote(final ConnectionInfo serverDetails) {
        // Try connect to the server
        try {
            client.connectToServer(serverDetails);
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to connect to server - see stack trace", true));
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
     * Server loop
     * @throws InterruptedException
     */
    private void serverLoop() throws InterruptedException{
        while(isPresenting.get()){
            try {
                // Accept the connection
                server.acceptConnection();
            } catch (SocketTimeoutException ex) {
                // Suspend for a while
                synchronized (this) {
                    wait(CS_TIMEOUT);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> engine.
                        putMessage("Fail to accept connection - see stack trace", true));
            }
        }

        // Close the server
        try {
            server.closeServer();
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to close server - see stack trace", true));
        }
    }

    /**
     * Client loop
     * @throws InterruptedException
     */
    private void clientLoop() throws InterruptedException{
        while(isConnected.get()){
            try {
                // Receive the event
                if(client.rxAvailable()){
                    Event event = (Event) client.readObject();
                    rxBufferQueue.offer(event);
                }
                if(!isPaused.get()){
                    while(!rxBufferQueue.isEmpty()){
                        Event event = rxBufferQueue.take();
                        engine.offerEvent(event);
                    }
                }
            } catch (SocketTimeoutException ex) {
                // Suspend for a while
                synchronized (this) {
                    wait(CS_TIMEOUT);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> engine.
                        putMessage("Fail to receive event - see stack trace", true));
            }
        }

        // Close the client
        try {
            client.disconnectFromServer();
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to close client - see stack trace", true));
        }
    }
}
