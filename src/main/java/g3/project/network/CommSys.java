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
     * Client.
     */
    private Client client;

    /**
     * Server.
     *
     * @param commSys
     */
    private Server server;

    /**
     * Server connection queue.
     *
     * @todo Replace String placeholder.
     */
    private final BlockingQueue<ConnectionInfo> serverConnectionQueue
            = new LinkedBlockingQueue<>();

    /**
     * Transmit queue. From Engine.
     */
    private final BlockingQueue<Event> txEventQueue
            = new LinkedBlockingQueue<Event>();

    /**
     * Received data queue. To Engine.
     */
    private final BlockingQueue<Event> rxEventQueue
            = new LinkedBlockingQueue<Event>();

    /**
     * Ref to the Engine.
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
     * Request to connect to a server.
     *
     * @todo Replace String placeholder.
     *
     * @param serverDetails Server to connect to.
     */
    public void requestConnection(final ConnectionInfo serverDetails) {
        serverConnectionQueue.offer(serverDetails);
        unsuspend();
    }

    /**
     * Start the server with the specified configuration.
     *
     * @param serverConfig server configuration.
     */
    public void startServer(final String serverConfig) {
        if (Thread.currentThread() != myThread) {
            //This enforces the thread boundary.
            runFunction(() -> startServer(serverConfig));
            return;
        }
        //Start server an' that.
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
            //DO NOT START CLIENTS/SERVERS BEFORE THEY ARE REQUIRED!
            //Init the client session
            //client.initClient();
            //Start the server session
            //server.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //...
        while (running.get()) {
            //Main thread dispatch loop
            try {
                if (!serverConnectionQueue.isEmpty()) { //New connection request?
                    connectToRemote(serverConnectionQueue.take());
                } else if (!txEventQueue.isEmpty()) { //Event to send?
                    transmitEvent(txEventQueue.take());
                } else if (!callQueue.isEmpty()) { //Something needs running.
                    callQueue.take().run();
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
     * Connect to the server.
     *
     * @todo Replace String placeholder.
     * @param serverDetails Remote server details.
     */
    private void connectToRemote(final ConnectionInfo serverDetails) {
        // Try connect to the server
        /*
        try {
            client.connectToServer(server);
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to connect to server - see stack trace", true));
        }*/
    }

    /**
     * Upload an event to the server.
     *
     * @param event Event to send.
     */
    private void transmitEvent(final Event event) {
        //TRANSMITTED (Server) EVENTS SHOULD NOT GO THROUGH A CLIENT!!!
        // Try to upload the event to the server
        /*
        try {
            client.sendObjectToServer(event);
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> engine.
                    putMessage("Fail to upload event to server - see stack trace", true));
        }
         */
    }

    /*THE ENGINE WILL HANDLE EVENTS DIRECTLY FROM THE RX_QUEUE
    PLEASE DO NOT FEED THEM IN IN THIS WAY.
    I'm purposefully using a blocking queue, as they're thread safe.
    Of course, if you really wanted, the constructor could take the engine's event queue as a param -
    but that'd not be so good. I want to be able to distinguish between local and remote events.
     */
    /**
     * Load an event to the engine from the server.
     *
     * @param event
     */
    /*
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
    }*/
}
