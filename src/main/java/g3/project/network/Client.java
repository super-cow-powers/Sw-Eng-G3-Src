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

import org.apache.commons.io.input.ObservableInputStream;

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
     * Recieve Check timeout in Seconds.
     */
    private static final int CHECK_TIMEOUT = 50;

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

    // Connect client to server
    public void connectToServer(final ConnectionInfo serverDetails) throws IOException{
        socket.setSoTimeout(CONNECT_TIMEOUT);
        socket.connect(new InetSocketAddress(serverDetails.getHostLoc(), serverDetails.getPort()), CONNECT_TIMEOUT);
        rxStream = new ObjectInputStream(socket.getInputStream());
        txStream = new ObjectOutputStream(socket.getOutputStream());
        readStream();
        writeStream("Connected");
    }

    //disconnect client from server
    public void disconnectFromServer(){
        try {
            writeStream("Disconnect");
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Error closing socket");
        }
    }

    /**
     * Read object from the input stream.
     */
    public Optional<Object> readStream() throws IOException {
        socket.setSoTimeout(READ_TIMEOUT);
        Optional<Object> rxObj = Optional.empty();
        try {
            rxObj = Optional.of(rxStream.readObject());
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rxObj.ifPresent(Obj -> System.out.println("Client: Received: " + Obj.toString()));
        return rxObj;
    }

    /**
     * Write object to the output stream.
     */
    public void writeStream(final Object txObj) throws IOException {
        txStream.writeObject(txObj);
        txStream.flush();
        System.out.println("Client: Sent: " + txObj.toString());
    }
}
