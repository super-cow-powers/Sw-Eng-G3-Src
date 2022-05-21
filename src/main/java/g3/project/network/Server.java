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
import java.util.Iterator;

import java.io.*;

/**
 *
 * @author Boris Choi<kyc526@york.ac.uk>
 */
public final class Server {

    private static final int ACCEPT_TIMEOUT = 50;

    private static final int CHECK_TIMEOUT = 100;

    private ServerSocket serverSocket;

    private static final int MAX_CLIENTS = 3;

    /**
     * List containing connected clients.
     */
    private ArrayList<Client> connectionsList = new ArrayList<>();

    /**
     * Constructor.
     * @throws IOException
     */
    public Server() throws IOException {
        serverSocket = new ServerSocket(0);
    }

    /**
     * Constructor - server socket on specific setup.
     * 
     * @param connectionRef server details
     * @throws IOException
     */
    public Server(ConnectionInfo connectionRef) throws IOException {
        serverSocket = new ServerSocket(connectionRef.getPort());
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
     * Accept client connection.
     * 
     * @throws IOException IO Error.
     */
    public void acceptConnection() throws IOException {
        serverSocket.setSoTimeout(ACCEPT_TIMEOUT);
        if (connectionsList.size() < MAX_CLIENTS) {
            var newClientSocket = serverSocket.accept();
            var txStream = new ObjectOutputStream(newClientSocket.getOutputStream()) ;
            var rxStream = new ObjectInputStream(newClientSocket.getInputStream());

            var newClient = new Client(newClientSocket, rxStream, txStream);
            connectionsList.add(newClient);

            newClient.readStream();
            newClient.writeStream("Welcome to the server!");
        }
    }

    /**
     * Close the server.
     * @throws IOException
     */
    public void closeServer() throws IOException {
        broadcastObject("Server: closing server");
        for (var client : connectionsList) {
            client.closeSocket();
        }
        connectionsList.clear();
        serverSocket.close();
    }

    /**
     * Send an object to all connected clients.
     * 
     * @param object Object to send.
     * @throws IOException IO Error.
     */
    public void broadcastObject(Object object) throws IOException {
        for (var client : connectionsList) {
            client.writeStream(object);          
        }
    }
    
    /**
     * Check if clients are still connected.
     * 
     * @throws IOException IO Error.
     */
    public void checkConnection() throws IOException {
        serverSocket.setSoTimeout(CHECK_TIMEOUT);
        Iterator<Client> iterator = connectionsList.iterator();
        while (iterator.hasNext()) {
            var client = iterator.next();
            var msg = client.readStream();
            msg.ifPresent(m->{
                if(m.equals("Disconnect")) {
                    System.out.println("Server: client disconnected");
                    client.closeSocket();
                    connectionsList.remove(client);
                }else{
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
        }
    }
}
