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
import java.security.PublicKey;
import java.util.ArrayList;
import java.io.*;
import java.lang.reflect.Array;

/**
 *
 * @author Boris Choi<kyc526@york.ac.uk>
 */
public class Server {
    private static final int PORT = 8080;

    private ServerSocket serverSocket;
    private int clientCnt = 0;
    private int clientCntMax = 10;

    private ArrayList<Socket> clientSockets = new ArrayList<>();
    private ArrayList<PublicKey> clientPublicKeys = new ArrayList<>();

    /**
     * Constructor - define max client count
     * 
     * @param clientCntMax
     */
    public Server(int clientCntMax) {
        this.clientCntMax = clientCntMax;
    }

    /**
     * Constructor - use default max client count
     */
    public Server() {
    }

    /**
     * Start the server
     * 
     * @throws IOException
     */
    public void initServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
    }

    /**
     * Accept client connection
     * 
     * @throws IOException
     */
    public void acceptConnection() throws IOException {
        if (clientCnt < clientCntMax) {
            Socket socket = serverSocket.accept();
            ObjectInputStream eventIn = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream eventOut = new ObjectOutputStream(socket.getOutputStream());

            int clientID = clientCnt++;
            eventOut.writeInt(clientID);

            clientSockets.add(socket);

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

    /**
     * Getter for port number
     * 
     * @return port number
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }
}
