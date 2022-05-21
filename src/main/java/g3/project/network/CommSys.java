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
    private static final int CS_PAUSE = 50;

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
    private AtomicBoolean isViewing = new AtomicBoolean(false);

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
                    update();
                }

                while (suspended.get()) { // Suspend
                    synchronized (this) {
                        wait();
                    }
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
            throw new IllegalArgumentException("Invalid connection type.");
        }
    }

    /**
     * Begin hosting a session.
     * 
     * @param serverDetails
     */
    private void startHosting(ConnectionInfo connectionRef){
        try{
            server = new Server(connectionRef);
            isViewing.set(false);
            isPresenting.set(true);
            isPaused.set(false);
        } catch (IOException ex){
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to start hosting", true));
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
            isViewing.set(true);
            isPresenting.set(false);
            isPaused.set(false);
        } catch (IOException ex){
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to start viewing", true));
        }
    }

    /**
     * Stop hosting a session.
     */
    public void stopHosting() {
        try{
            if (server != null) {
                server.closeServer();
                server = null;
                isViewing.set(false);
                isPresenting.set(false);
                isPaused.set(true);
            }
        } catch (IOException ex){
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to stop hosting", true));
        }
    }
    
    /**
     * Stop viewing a session
     */
    private void stopViewing(){
        if (client != null) {
            client.disconnectFromServer();
            client = null;
            isViewing.set(false);
            isPresenting.set(false);
            isPaused.set(true);
        }
    }

    /**
     * Toggle the connection status
     */
    private void sessionUpdate() {
        isPaused.set(!isPaused.get());
    }

    /**
     * Upload an event to the server.
     *
     * @param event Event to send.
     * @throws InterruptedException
     */
    private void transmitEvent(final Event event) throws InterruptedException {
        try {
            txBufferQueue.offer(event);
            if (!isPaused.get()) {
                while(!txBufferQueue.isEmpty()) {
                    server.broadcastObject(txBufferQueue.take());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to upload event to server", true));
        }
        
    }

    /**
     * Server try to first accept a connection.
     * Then it will try to check if all the clients are still connected.
     * 
     * @throws IOException
     */
    private void serverCheck() throws IOException{
        try {
            server.acceptConnection();
        } catch (SocketTimeoutException ste) {
            server.checkConnection();
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to accept connection", true));
        }
    }

    /**
     * Client try to read from the server.
     * If server has shut down, close the client.
     * Otherwise read from input stream and put into the buffer queue.
     * Only if the client is not paused should the event be sent to the engine.
     * 
     * @throws InterruptedException
     */
    private void clientCheck() throws InterruptedException, SocketTimeoutException{
        try {
            var objectIn = client.readStream();
            objectIn.ifPresent(obj -> {
                if (!obj.equals("Server: closing server")) {
                    rxBufferQueue.offer((Event) obj);
                } else {
                    stopViewing();
                }
            });
            if(!isPaused.get()){
                // Update local session
                while(!rxBufferQueue.isEmpty()){
                    Event event = rxBufferQueue.take();
                    engine.offerEvent(event);
                }
            }
        } catch (SocketTimeoutException ste) {
            throw ste;
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to receive event from server", true));
        }
    }

    /**
     * Comm-System Update
     * 
     * @throws InterruptedException
     */
    public void update() throws InterruptedException {
        try{
            if (isViewing.get()) {
                clientCheck();
            } else if (isPresenting.get()) {
                serverCheck();
            } else{
                suspended.set(true); // Nothing from enngine
            }
        }catch (SocketTimeoutException ste){
            Thread.sleep(CS_PAUSE);
        }catch (IOException ex){
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to check client connection", true));
        }
        
    }

    /**
     * Terminate commSys cleanly.
     */
    public void terminate() {
        stopHosting();
        stopViewing();
    }
}
