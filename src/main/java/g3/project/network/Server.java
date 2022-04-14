/*
 * Copyright (c) 2022, Boris Choi<kyc526@york.ac.uk>
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

import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import g3.project.core.Threaded;
import javafx.event.Event;

import java.io.*;

/**
 *
 * @author Boris Choi<kyc526@york.ac.uk>
 */
public final class Server {

    private static final int SERVER_TIMEOUT = 10000;

    private ServerSocket serverSocket;

    private String hostID = "";

    private static final int MAX_CLIENTS = 10;

    //private ArrayList<Client> clients = new ArrayList<>();
    /**
     * List containing connected clients. STRING IS A PLACEHOLDER!
     */
    private ArrayList<Socket> connectionsList = new ArrayList<>();

    /**
     * Event transmit queue.
     */
    private BlockingQueue<Event> transmitQueue
            = new LinkedBlockingQueue<>();

    /**
     * Client connection request queue.
     *
     */
    private BlockingQueue<String> connectionRequestQueue
            = new LinkedBlockingQueue<>();

    /**
     * Constructor.
     */
    public Server() {
    }

    /**
     * Host offer an object to the server queue.
     *
     * @param event Event to send.
     */
    public void sendEvent(final Event event) {
        transmitQueue.offer(event);
    }

    /**
     * Getter for port number.
     *
     * @return port number.
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * Getter for server remote socket address.
     *
     * @return server socket address.
     */
    public InetAddress getAddress() {
        return serverSocket.getInetAddress();
    }

    /**
     * Start the server.
     *
     * @throws IOException IO Error.
     */
    public void initServer() throws IOException {
        serverSocket = new ServerSocket(0);
        serverSocket.setSoTimeout(SERVER_TIMEOUT);
    }

    /**
     * Accept client connection.
     *
     * @todo I'm not sure if this is the correct way to handle multiple
     * connections to a simple socket-based server. Needs looking-up.
     * 
     * @throws Exception Exception.
     */
    public void acceptConnection() throws Exception {
        if (connectionsList.size() < MAX_CLIENTS) {
            //Accept client connection
            Socket newClientSocket = serverSocket.accept();

            //Add socket to connections
            connectionsList.add(newClientSocket);
        } else {
            System.out.println("Server is full");
        }
    }

    /**
     * Close the server.
     *
     * @throws IOException IO Error.
     */
    public void closeConnection() throws IOException {
        serverSocket.close();
    }
}
