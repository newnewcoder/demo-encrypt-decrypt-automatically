package com.example.demo.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AesUtilTest {
    @Test
    public void encrypt_decrypt_test() {
        String plainTxt = "你好嗎我不好";
        String encrypted = AesUtil.encrypt(plainTxt);
        String decrypted = AesUtil.decrypt(encrypted);
        System.out.println("plainTxt=" + plainTxt);
        System.out.println("encrypted=" + encrypted);
        System.out.println("decrypted=" + decrypted);
        Assertions.assertEquals(decrypted, plainTxt);
    }
}
