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
import java.util.Optional;
import java.io.*;

/**
 *
 * @author Boris Choi<kyc526@york.ac.uk>
 */
public final class Client{
    /**
     * Connection timeout in Seconds.
     */
    private static final int CONNECT_TIMEOUT = 10000;

    /**
     * Read timeout in Seconds.
     */
    private static final int READ_TIMEOUT = 100;

    /**
     * My connection.
     */
    private Socket socket;

    /**
     * Client Received-data stream.
     */
    private ObjectInputStream rxStream;

    /**
     * Client Transmit-data stream.
     */
    private ObjectOutputStream txStream;

    /**
     * Constructor - Initialise the client object.
     *
     * @param server Details of server to connect to.
     * @throws IOException
     */
    public Client() throws IOException {
        this.socket = new Socket();
    }

    /**
     * Constructor - Initialise the client object for a pre-existing socket.
     * 
     * @param socket Pre-existing socket.
     * @param rxStream Pre-existing receive stream.
     * @param txStream Pre-existing transmit stream.
     * @throws IOException
     */
    public Client(Socket socket, ObjectInputStream rxStream, ObjectOutputStream txStream) throws IOException {
        this.socket = socket;
        this.rxStream = rxStream;
        this.txStream = txStream;
    }

    /**
     * Connect to the server.
     *
     * @param server Details of server to connect to.
     * @throws IOException
     */
    public void connectToServer(final ConnectionInfo serverDetails) throws IOException{
        socket.setSoTimeout(CONNECT_TIMEOUT);
        socket.connect(new InetSocketAddress(serverDetails.getHostLoc(), serverDetails.getPort()), CONNECT_TIMEOUT);
        rxStream = new ObjectInputStream(socket.getInputStream());
        txStream = new ObjectOutputStream(socket.getOutputStream());
        writeStream("Connected");
        readStream();
    }

    /**
     * Disconnect from the server.
     */
    public void disconnectFromServer(){
        try {
            writeStream("Disconnect");
            closeSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close socket.
     */
    public void closeSocket(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read object from the input stream if any.
     * 
     * @return Optional object.
     * @throws IOException
     */
    public Optional<Object> readStream() throws IOException {
        socket.setSoTimeout(READ_TIMEOUT);
        Optional<Object> rxObj = Optional.empty();
        try {
            rxObj = Optional.of(rxStream.readObject());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return rxObj;
    }

    /**
     * Write object to the output stream.
     * 
     * @param obj Object to write.
     */
    public void writeStream(final Object txObj) throws IOException {
        txStream.writeObject(txObj);
        txStream.flush();
    }
}
