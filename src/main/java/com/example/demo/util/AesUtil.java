package com.example.demo.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.util.DigestUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AesUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS7Padding";

    private static final String keyStr = "0123456789abcdefghij0123456789ab";

    public static String encrypt(String data) {
        try {
            return URLEncoder.encode(
                    Base64.getEncoder().encodeToString(encrypt(data.getBytes(StandardCharsets.UTF_8))),
                    StandardCharsets.UTF_8.toString()
            );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error occurred.", e);
        }
    }

    public static String decrypt(String encryptedData) {
        if (encryptedData == null || "".equals(encryptedData)) {
            return null;
        }
        try {
            byte[] dataByte = Base64.getDecoder().decode(
                    URLDecoder.decode(encryptedData, StandardCharsets.UTF_8.toString())
            );
            return new String(decrypt(dataByte));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error occurred.", e);
        }
    }

    public static byte[] encrypt(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, new BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), getIv());
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("Error occurred.", e);
        }
    }

    public static byte[] decrypt(byte[] encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, getKey(), getIv());
            return cipher.doFinal(encryptedData);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("Error occurred.", e);
        }
    }

    private static SecretKeySpec getKey() {
        byte[] keyByte = keyStr.getBytes(StandardCharsets.US_ASCII);
        SecretKeySpec key = new SecretKeySpec(keyByte, "AES");
        return key;
    }

    private static IvParameterSpec getIv() {
        byte[] keyByte = keyStr.getBytes(StandardCharsets.US_ASCII);
        IvParameterSpec iv = new IvParameterSpec(DigestUtils.md5Digest(keyByte));
        return iv;
    }
}