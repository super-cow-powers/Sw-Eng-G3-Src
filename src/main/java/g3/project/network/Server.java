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
public class Server extends Threaded {
    private static final int SERVER_TIMEOUT = 10000;

    private ServerSocket serverSocket;
    private int hostID = 0;
    private int clientCnt = 0;
    private int clientCntMax = 10;

    private ArrayList<Client> clients = new ArrayList<Client>();

    /**
     * Host upload queue.
     */
    private BlockingQueue<Object> uploadQueue
            = new LinkedBlockingQueue<Object>();

    /**
     * Client connection request queue.
     */
    private BlockingQueue<Event> connectToServerRequestQueue
            = new LinkedBlockingQueue<Event>();

    private final CommSys commSys;

    /**
     * Constructor - use default max client count
     */
    public Server(final CommSys commSys) {
        super();
        this.commSys = commSys;
    }

    /**
     * Host offer an object to the server queue
     */
    public void offerEventToUpload(Object event) {
        uploadQueue.offer(event);
        unsuspend();
    }

    /**
     * Getter for port number
     * 
     * @return port number
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * Getter for server remote socket address
     * 
     * @return server socket address
     */
    public InetAddress getAddress() {
        return serverSocket.getInetAddress();
    }

    @Override
    public void run() {
        while (!(running.get())){
        }
        //Post-construction Setup goes here

        //...
        while (running.get()) {
            try {
                if (!connectToServerRequestQueue.isEmpty()) {//New connection request?

                } else if (!uploadQueue.isEmpty()) {//New object being uploaded?

                } else { // No new event. Suspend.
                    suspended.set(true);
                }

                while (suspended.get()) {
                    synchronized (this) {
                        wait();
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }        

    /**
     * Start the server
     * 
     * @throws IOException
     */
    public void initServer() throws IOException {
        serverSocket = new ServerSocket(0);
        serverSocket.setSoTimeout(SERVER_TIMEOUT);
    }

    /**
     * Accept client connection
     * 
     * @throws IOException
     */
    public void acceptConnection() throws Exception {
        if (clientCnt < clientCntMax) {
            //Accept client connection
            Socket newClientSocket = serverSocket.accept();

            //Clone client socket and IO streams
            Client newClient = new Client();
            newClient.setSocket(newClientSocket);
            
            //Add clone to client list
            clients.add(newClient);
        } else {
            System.out.println("Server is full");
        }
    }

    /**
     * Close the server
     */
    public void closeConnection() throws IOException {
        serverSocket.close();
    }
}
