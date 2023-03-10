package com.kdm1t.digsig.Tools;

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeysTools {

    public static final String RSA = "RSA";
    public static final String KEYS_PATH = "Z:/keys/";

    public static void generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA);
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        FileTools.writeFile(KEYS_PATH + "public.key", pair.getPublic().getEncoded());
        FileTools.writeFile(KEYS_PATH + "private.key", pair.getPrivate().getEncoded());
    }

    public static void updateKeyFactory() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);

        byte[] publicKeyBytes = Files.readAllBytes(Path.of(KEYS_PATH + "public.key"));
        byte[] privateKeyBytes = Files.readAllBytes(Path.of(KEYS_PATH + "private.key"));

        keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
    }

    public static RSAPublicKey readPublicKey() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);

        byte[] keyBytes = Files.readAllBytes(Path.of(KEYS_PATH + "public.key"));
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(keyBytes);
        return (RSAPublicKey) keyFactory.generatePublic(pubSpec);


    }

    public static RSAPrivateKey readPrivateKey() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);

        byte[] keyBytes = Files.readAllBytes(Path.of(KEYS_PATH + "private.key"));
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(keyBytes);

        return (RSAPrivateKey) keyFactory.generatePrivate(privSpec);
    }

    public static byte[] getEncryptedHash(String message) throws Exception {
        Cipher encryptCipher = Cipher.getInstance(RSA);

        encryptCipher.init(Cipher.ENCRYPT_MODE, KeysTools.readPrivateKey());
        byte[] hash = DigestUtils.md5Hex(message).getBytes(StandardCharsets.UTF_8);
        return encryptCipher.doFinal(hash);
    }

    public static byte[] getDecryptedHash(String path) throws Exception {
        Cipher decryptCipher = Cipher.getInstance(RSA);
        decryptCipher.init(Cipher.DECRYPT_MODE, KeysTools.readPublicKey());
        byte[] hashFromFile = Files.readAllBytes(Path.of(path));
        return decryptCipher.doFinal(hashFromFile);
    }

}
