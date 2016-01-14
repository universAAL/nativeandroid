/*
	Copyright 2015 ITACA-SABIEN, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (SABIEN)
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.android.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.content.Context;

/**
 * A utility class for encrypting/decrypting strings
 */
public class CryptUtil {

    private static final String cipherTransformation = "DES/ECB/PKCS5Padding";
    private static final String secretKeyAlgorithm = "DES";

    private static SecretKey skey = null;

    /**
     * Initialization method - reads the shared key from the file system
     * 
     * @param String
     *            key - full path to encryption key including file name
     */
    public static String init(Context ctxt) throws Exception {
	skey = readKey(ctxt);
	if (skey == null) {
	    throw new SecurityException(
		    "Missing the secret key for message exchange!");
	}
	return "Cryptography utils initialized successfully!";
    }
    
    public static boolean isInit(){
	return skey!=null;
    }

    /**
     * decrypt the parameter string with the shared key read during
     * initialization
     * 
     * @param String
     *            chiper - the string to decrypt
     * @return the decrypted string
     * 
     */
    public static String decrypt(String cipher) throws Exception {
	return decrypt(cipher, skey);
    }

    /**
     * decrypt the first parameter string with the shared key received as the
     * second parameter
     * 
     * @param String
     *            chiper - the string to decrypt
     * @param SecretKey
     *            skey - the shared key
     * @return the decrypted string
     * 
     */
    public static String decrypt(String cipher, SecretKey skey)
	    throws Exception {
	Cipher desCipher = Cipher.getInstance(cipherTransformation);
	desCipher.init(Cipher.DECRYPT_MODE, skey);
	return new String(desCipher.doFinal(decode(cipher)));
    }

    /**
     * encrypt the parameter string with the shared key read during
     * initialization
     * 
     * @param String
     *            clear - the string to encrypt
     * @return the encrypted string
     * 
     */
    public static String encrypt(String clear) throws Exception {
	return encrypt(clear, skey);
    }

    /**
     * encrypt the first parameter string with the shared key received as the
     * second parameter
     * 
     * @param String
     *            clear - the string to encrypt
     * @param SecretKey
     *            skey - the shared key
     * @return the encrypted string
     * 
     */
    public static String encrypt(String clear, SecretKey skey)
	    throws Exception {
	Cipher desCipher = Cipher.getInstance(cipherTransformation);
	desCipher.init(Cipher.ENCRYPT_MODE, skey);
	return new String(encode(desCipher.doFinal(clear.getBytes())));
    }

    private static SecretKey readKey(Context ctxt)  throws Exception {
        FileInputStream fis = ctxt.openFileInput(AppConstants.GCM_ENCRYPT_KEYFILE);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int read;
        byte[] data = new byte[4096];
        while ((read = fis.read(data, 0, data.length)) != -1) {
          bos.write(data, 0, read);
        }
        bos.flush();
        byte[] rawkey = bos.toByteArray();
        bos.close();
        fis.close();
        return genKey(rawkey);
    }
    
    public static SecretKey genKey(byte[] rawkey) throws Exception{
	DESKeySpec keyspec = new DESKeySpec(rawkey);
	SecretKeyFactory keyfactory = SecretKeyFactory
		.getInstance(secretKeyAlgorithm);
	return keyfactory.generateSecret(keyspec);
    }

    private static byte[] encode(byte[] data) {
	return org.bouncycastle.util.encoders.Base64.encode(data);
    }

    private static byte[] decode(String data) {
	return org.bouncycastle.util.encoders.Base64.decode(data);
    }

}
