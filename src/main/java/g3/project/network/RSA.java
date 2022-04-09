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

import java.io.Serializable;
import java.security.*;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;

/**
 *
 * @author Boris Choi<
 */ 
public class RSA {
    private String algorithm = "RSA";
    private String transformation = "RSA/ECB/PKCS1Padding";
    private int keySize = 1024;

    private PrivateKey privateKey;
    private PublicKey publicKey;


    /**
     * Constructor - define the key size and algorithm
     * 
     * @param algorithm
     * @param transformation
     * @param keySize
     */
    public RSA(String algorithm, String transformation, int keySize) {
        this.algorithm = algorithm;
        this.transformation = transformation;
        this.keySize = keySize;
    }

    /**
     * Constructor - use default key size and algorithm
     */
    public RSA() {

    }


    /**
     * Generate a pair of keys.
     */
    public void createKeyPair(){
        try{
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
            keyGen.initialize(keySize);
            KeyPair pair = keyGen.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the public key.
     * 
     * @return The public key.
     */
    public PublicKey getPublicKey(){
        return publicKey;
    }

    /**
     * Encrypts an object using the public key.
     * 
     * @param objectToEncrypt
     * @param targetPublicKey
     * @return encryptedObject
     * @throws Exception
     */
    public SealedObject encryptPublic(Serializable objectToEncrypt, PublicKey recieverPublicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, recieverPublicKey);
        return new SealedObject(objectToEncrypt, cipher);
    }

    /**
     * Encrypts an object using the private key.
     * 
     * @param objectToEncrypt
     * @param selfPrivateKey
     * @return encryptedObject
     * @throws Exception
     */
    public SealedObject encryptPrivate(Serializable objectToEncrypt) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return new SealedObject(objectToEncrypt, cipher);
    }

    /**
     * Decrypts an object using the private key.
     * 
     * @param encryptedObject
     * @return decryptedObject
     * @throws Exception
     */
    public Serializable decryptPrivate(SealedObject encryptedObject) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return (Serializable) encryptedObject.getObject(cipher);
    }

    /**
     * Decrypts an object using the public key.
     * 
     * @param encryptedObject
     * @return decryptedObject
     * @throws Exception
     */
    public Serializable decryptPublic(SealedObject encryptedObject, PublicKey senderPublicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, senderPublicKey);
        return (Serializable) encryptedObject.getObject(cipher);
    }
}
