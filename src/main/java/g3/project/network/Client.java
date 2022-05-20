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

import org.apache.commons.io.input.ObservableInputStream;

/**
 *
 * @author Boris Choi<kyc526@york.ac.uk>
 */
public final class Client{

    /**
     * Connection timeout in Seconds.
     */
    private static final int CLIENT_TIMEOUT = 10000;
    /**
     * My connection.
     */
    private Socket socket;

    /**
     * Client Received-data stream.
     */
    private ObjectInputStream rxStream;


    //private ObservableInputStream ois;

    /**
     * Constructor - Initialise the client object.
     *
     * @param server Details of server to connect to.
     * @throws IOException
     */
    public Client() throws IOException {
        this.socket = new Socket();
        socket.setSoTimeout(CLIENT_TIMEOUT);
    }

    // Connect client to server
    public void connectToServer(final ConnectionInfo serverDetails) throws IOException{
        socket.connect(new InetSocketAddress(serverDetails.getHostLoc(), serverDetails.getPort()), CLIENT_TIMEOUT);
        rxStream = new ObjectInputStream(socket.getInputStream());
    }

    //disconnect client from server
    public void disconnectFromServer() throws IOException{
        socket.close();
    }

    /**
     * See if object is available to read from the input stream.
     */
    public boolean rxAvailable() throws IOException {
        return rxStream.available() > 0;
    }

    /**
     * Read object from the input stream.
     */
    public Object readObject() throws IOException, ClassNotFoundException {
        return rxStream.readObject();
    }

    /**
     * Close the client.
     */
    public void close() throws IOException {
        socket.close();
    }
}
