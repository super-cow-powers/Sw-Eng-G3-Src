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

import java.io.*;

/**
 *
 * @author Boris Choi<kyc526@york.ac.uk>
 */
public class Client {
    private static final int CLIENT_TIMEOUT = 10000;
    private Socket socket;

    private ObjectInputStream rxStream;
    private ObjectOutputStream txStream;

    /**
     * Constructor - creates a new client object
     * @throws IOException
     */
    public Client() {
    }

    /**
     * Initialise the client object
     * 
     * @throws IOException
     */
    public void initClient() throws IOException{
        setSocket(new Socket());
    }

    /**
     * Set socket and IO streams
     * 
     * @param socket
     * @throws IOException
     */
    public void setSocket(Socket socket) throws IOException {
        this.socket = socket;
        rxStream = new ObjectInputStream(socket.getInputStream());
        txStream = new ObjectOutputStream(socket.getOutputStream());
    }

    public ObjectInputStream getRxStream(){
        return rxStream;
    }

    public ObjectOutputStream getTxStream(){
        return txStream;
    }

    // Connect client to server
    public void connectToServer(Server server) throws IOException{
        socket.connect(new InetSocketAddress(server.getAddress(), server.getPort()), CLIENT_TIMEOUT);
    }

    //disconnect client from server
    public void disconnectFromServer() throws IOException{
        socket.close();
    }

    // Send object to server
    public void sendObjectToServer(Object object) throws IOException {
        txStream.writeObject(object);
        txStream.flush();
    }

    // Read object from server
    public Object readObjectFromServer() throws IOException, ClassNotFoundException {
        return rxStream.readObject();
    }
}