/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.fileupload;

import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.fileupload.util.crypto.CipherServiceProvider;

public class TestCipherProvider implements CipherServiceProvider.CipherProvider {
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    
    private KeySpec keySpec;
    private SecretKey tmp;
    private SecretKey secret;
    private byte[] ivBytes;

    public TestCipherProvider() {
        super();
        try {
            keySpec = new PBEKeySpec("secret".toCharArray(), "salt".getBytes(), 1, 256);
            tmp = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(keySpec);
            secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("TestCipherProvider doesn't initilized!", e);
        }
    }

    @Override
    public Cipher getEncryptionCipher() {

        try {
            Cipher ecipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            ecipher.init(Cipher.ENCRYPT_MODE, secret);
            AlgorithmParameters params = ecipher.getParameters();
            this.ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();

            return ecipher;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Encryption cipher doesn't initilized!", e);
        } 
    }

    @Override
    public Cipher getDecryptionCipher() {

        try {
            Cipher dcipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            dcipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));

            return dcipher;

        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Decryption Cipher doesn't initilized!", e);
        } 
    }
}
