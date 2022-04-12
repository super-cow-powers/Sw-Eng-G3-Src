/*
 * Copyright (c) 2022, David Miall<dm1306@york.ac.uk>
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
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class NetThing extends Threaded {
    /**
     * Client Session
     */
    private final Client client = new Client();

    /**
     * Server Session
     */
    private final Server server = new Server(this);

    /**
     * Am I hosting?
     */
    private final AtomicBoolean isHosting = new AtomicBoolean(false);

    /**
     * Am I connected to a session?
     */
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    /**
     * Connect to Server request queue.
     */
    private final BlockingQueue<Event> connectToServerRequestQueue 
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
    public NetThing(final Engine engine) {
        super();
        this.engine = engine;
    }

    /**
     * Request to connect to Server.
     * 
     * @param event Event to connect to server
     */
    public void connectToServer(final Event event) {
        connectToServerRequestQueue.offer(event);
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
        initNetThing();

        //...
        while (running.get()) {
            //Main thread dispatch loop
            try {
                if (!connectToServerRequestQueue.isEmpty()) {
                    if(!isConnected.get()) {
                        //Connect to Server
                    }
                } else if (!txEventQueue.isEmpty()) {
                    if(isHosting.get()) {
                        //send to server
                    }
                } else if (!rxEventQueue.isEmpty()) {
                    if(isConnected.get()) {
                        //receive from server
                    }
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

        try {
            server.closeConnection();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Net-thing is going down NOW.");
        return;
    }

    private void initNetThing() {
        // Initialise the Client
        // TODO Load from file?
        try {
            client.initClient();
        } catch (IOException initCE) {
            // TODO Auto-generated catch block
            initCE.printStackTrace();
            Platform.runLater(() -> engine.
            putMessage("Failed to initialise client", false));
        }
        // Initialise the Server
        // TODO Load from file?
        try {
            server.initServer();
            server.checkInHost(client);
        } catch (IOException initSE) {
            // TODO Auto-generated catch block
            initSE.printStackTrace();
            Platform.runLater(() -> engine.
            putMessage("Failed to initialise server", false));
        }
    }

    /**
     * Encrypt and send an event to the server.
     * 
     * @throws IOException
     */
    private void txEvent(final Event event) throws IOException {
        try {
            // offer the event to the server to be sent
            server.offerTxEvent(event);
        } catch (Exception txE) {
            // TODO Auto-generated catch block
            txE.printStackTrace();
            Platform.runLater(() -> engine.
            putMessage("Failed to transfer event to server - see stack trace", false));
        }
    }

    /**
     * Recieve an encrypted event from the server and decrypt it.
     * 
     * @throws IOException
     */
    private void rxEvent() throws IOException {
        try {
            // receive event
            var eventLine = client.readObjectFromServer();
            // decrypt event
            var updateRx = client.decryption(eventLine);
            // send event to engine for processing
            // return to engine threadngine.
            engine.offerEvent((Event) updateRx);
        } catch (Exception rxE) {
            // TODO Auto-generated catch block
            rxE.printStackTrace();
            Platform.runLater(() -> engine.
            putMessage("Failed to recieve event from server - see stack trace", false));
        }
    }

}
