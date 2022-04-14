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

    /**
     * Client Session
     */
    private final Client client = new Client();

    /**
     * Server Session
     *
     * @param commSys
     */
    private final Server server = new Server(this);

    /**
     * Server connection action queue.
     */
    private final BlockingQueue<InetSocketAddress> viewConnectionQueue
            = new LinkedBlockingQueue<InetSocketAddress>();

    /**
     * Host session request queue.
     */
    private final BlockingQueue<Event> hostSessionQueue
            = new LinkedBlockingQueue<>();

    /**
     * Client to Server transfer queue.
     */
    private final BlockingQueue<Event> txEventQueue
            = new LinkedBlockingQueue<Event>();

    /**
     * Server to Client transfer queue.
     */
    private final BlockingQueue<Event> rxEventQueue
            = new LinkedBlockingQueue<Event>();

    /**
     * Ref to the Engine
     */
    private final Engine engine;

    /**
     * Constructor.
     */
    public CommSys(final Engine engine) {
        super();
        this.engine = engine;
    }

    /**
     * Send a connection event to the communication system.
     *
     * @param server Server to connect to.
     */
    public void offerConnectionEvent(final InetSocketAddress connectionRef) {
        viewConnectionQueue.offer(connectionRef);
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

    /**
     * Get the RX queue.
     *
     * @return blocking queue for events.
     */
    public BlockingQueue<Event> getRxQueue() {
        return rxEventQueue;
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
        //Wait till running properly
        while (!(running.get())) {
        }
        //Post-construction Setup goes here
        try {
            //Init the client session
            client.initClient();
            //Start the server session
            server.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            server.stop();
            return;
        }

        //...
        while (running.get()) {
            //Main thread dispatch loop
            try {
                if (!viewConnectionQueue.isEmpty()) { //New connection request?
                    connectionUpdate(viewConnectionQueue.take());
                } else if (!txEventQueue.isEmpty()) { //Event to send?
                    uploadToServer(txEventQueue.take());
                } else if (!rxEventQueue.isEmpty()) { //Event recieved?
                    loadUpdateToEngine(rxEventQueue.take());
                } else {
                    suspended.set(true);
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

        System.out.println("Comm-System is going down NOW.");
        return;
    }


    
    /**
     * Alter the connection status of the server.
     * 
     * @param event
     */
    private void connectionUpdate(final InetSocketAddress connectionRef) {
        // Route Event

    }

    /**
     * Upload an event to the server.
     *
     * @param event
     */
    private void uploadToServer(Event event) {
        // Try to upload the event to the server
        try {
            client.sendObjectToServer(event);
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to upload event to server - see stack trace", true));
        }
    }

    /**
     * Load an event to the engine from the server.
     *
     * @param event
     */
    private void loadUpdateToEngine(Event event) {
        // Try to load the event to the engine
        try {
            Event update = (Event) client.readObjectFromServer();
            engine.offerEvent(update);
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to load event to engine - see stack trace", true));
        }
    }
}
