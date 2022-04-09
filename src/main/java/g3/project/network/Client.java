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

import javafx.application.Application;
import java.net.*;
import java.security.PublicKey;

import javax.crypto.SealedObject;

import javafx.event.Event;

import java.io.*;

/**
 *
 * @author Boris Choi<kyc526@york.ac.uk>
 */
public class Client {

    private int clientID;
    private String host = "localhost";
    private int port = 5555;
    private Socket socket;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private RSA rsa;
    private PublicKey hostPublicKey;

    /**
     * Constructor - create a new client with the given host and port
     *
     * @param host
     * @param port
     */
    public Client(String host, int port){
        this.host = host;
        this.port = port;
    }

    /**
     * Constructor - creates a new client object with default host and port
     * 
     * @param args the command line arguments
     */
    public Client(){

    }

    // Connect client to server
    public void connectToServer() throws IOException {
        socket = new Socket(host, port);
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        rsa = new RSA();
        rsa.createKeyPair();
        clientID = in.readInt();

        // Send public key to server
        out.writeObject(rsa.getPublicKey());
        out.flush();
        
        // Get host public key
        try {
            hostPublicKey = (PublicKey) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // share public key with client
    private PublicKey sharePublicKey(){
        return rsa.getPublicKey();
    }

    // Send object to server
    public void sendObjectToServer(SealedObject object) throws IOException {
        out.writeObject(object);
        out.flush();
    }

    // Read object from server
    public SealedObject readObjectFromServer() throws IOException, ClassNotFoundException {
        return (SealedObject) in.readObject();
    }

    public SealedObject encryption(Serializable objectToEncrypt) throws IOException {
        try {
            //encrypt object with self private key
            SealedObject semiEncrptedObject = rsa.encryptPrivate(objectToEncrypt);
            //encrypt object with host public key
            SealedObject encryptedObject = rsa.encryptPublic(semiEncrptedObject, hostPublicKey);
            return encryptedObject;
        }   catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Serializable decryption(SealedObject objectToDecrypt) throws IOException {
        try {
            //decrypt object with self private key
            SealedObject semiDecryptedObject = (SealedObject) rsa.decryptPrivate(objectToDecrypt);
            //decrypt object with host public key
            Serializable decryptedObject = rsa.decryptPublic(semiDecryptedObject, hostPublicKey);
            return decryptedObject;
        }   catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
