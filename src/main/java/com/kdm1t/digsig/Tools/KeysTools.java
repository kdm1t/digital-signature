package com.kdm1t.digsig.Tools;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeysTools {

    public static final String RSA = "RSA";
    public static final String KEYS_PATH = "Z:/keys/";

    public static void generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        FileTools.writeFile(KEYS_PATH + "public.key", pair.getPublic().getEncoded());
        FileTools.writeFile(KEYS_PATH + "private.key", pair.getPrivate().getEncoded());
    }

    public static void updateKeyFactory(String algorithm) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File publicKeyFile = new File(KEYS_PATH + "public.key");
        File privateKeyFile = new File(KEYS_PATH + "private.key");
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
    }

    public static RSAPublicKey readPublicKey() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);

        File publicKey = new File(KEYS_PATH + "public.key");
        byte[] keyBytes =Files.readAllBytes(Path.of(KEYS_PATH + "public.key"));
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(keyBytes);
        return (RSAPublicKey) keyFactory.generatePublic(pubSpec);


    }

    public static RSAPrivateKey readPrivateKey() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        File privateKey = new File(KEYS_PATH + "private.key");

        byte[] keyBytes =Files.readAllBytes(Path.of(KEYS_PATH + "private.key"));
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(keyBytes);
        return (RSAPrivateKey) keyFactory.generatePrivate(privSpec);
    }

}
