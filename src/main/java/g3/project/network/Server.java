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
import javafx.event.Event;

import java.io.*;

/**
 *
 * @author Boris Choi<kyc526@york.ac.uk>
 */
public final class Server {

    private static final int SERVER_TIMEOUT = 50;

    private ServerSocket serverSocket;

    private static final int MAX_CLIENTS = 10;

    /**
     * List containing connected clients.
     */
    private ArrayList<Socket> connectionsList = new ArrayList<>();

    /**
     * List containing connected clients output stream.
     */
    private ArrayList<ObjectOutputStream> txStreamList = new ArrayList<>();

    /**
     * Constructor.
     * @throws IOException
     */
    public Server() throws IOException {
        serverSocket = new ServerSocket(0);
        serverSocket.setSoTimeout(SERVER_TIMEOUT);
    }

    /**
     * Constructor - server socket on specific setup.
     * @throws IOException
     */
    public Server(ConnectionInfo connectionRef) throws IOException {
        serverSocket = new ServerSocket(connectionRef.getPort());
        serverSocket.setSoTimeout(SERVER_TIMEOUT);
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
     * @throws IOException
     *
     * @todo I'm not sure if this is the correct way to handle multiple
     * connections to a simple socket-based server. Needs looking-up.
     * 
     * @throws Exception Exception.
     */
    public void acceptConnection() throws IOException {
        if (connectionsList.size() < MAX_CLIENTS) {
            //Accept client connection
            Socket newClientSocket = serverSocket.accept();
            
            var txStream = new ObjectOutputStream(newClientSocket.getOutputStream()) ;
            txStream.writeObject("Welcome to the server!");
            txStream.flush();     

            //Add connections to list
            connectionsList.add(newClientSocket);
            txStreamList.add(txStream);
            System.out.println("Server: new connection accepted");
        } else {
            System.out.println("Server: server is full");
        }
    }

    /**
     * Close the server.
     *
     * @throws IOException IO Error.
     */
    public void close() {
        try{
            System.out.println("Server: closing server");
            sendObject("Server: closing server");
            for (var txStream : txStreamList)  {
                txStream.close();
            }
            for (var client : connectionsList) {
                client.close();
            }
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Server: error closing server");
        }
    }

    /**
     * Send an event to all connected clients.
     * 
     * @param object Object to send.
     * @throws IOException IO Error.
     */
    public void sendObject(Object object) throws IOException {
        for (var txStream : txStreamList) {
            System.out.println("Server: sending object");
            txStream.writeObject(object);
            txStream.flush();            
        }
    }
}
