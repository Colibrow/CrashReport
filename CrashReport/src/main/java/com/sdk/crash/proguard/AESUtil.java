package com.sdk.crash.proguard;

import android.util.Base64;

import com.sdk.crash.BuildConfig;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {
    // 采用对称分组密码体制,密钥长度的最少支持为128、192、256
   private static String keyInfo = BuildConfig.keyInfo;
    // 初始化向量参数，AES 为16bytes. DES 为8bytes， 16*8=128
    private String initVector = "0000000000000000";
    private IvParameterSpec iv;
    private SecretKeySpec skeySpec;
    private Cipher cipher;

    static {
//        StringBuilder builder = new StringBuilder();
//        builder.append(Build.MANUFACTURER);
//        builder.append(Build.MODEL);
//        String phoneInfo = builder.toString();
//        if (phoneInfo.length() > 16) {
//            keyInfo = phoneInfo.substring(0, 16);
//        } else {
//            while (keyInfo.length() < 16) {
//                keyInfo += "0";
//            }
//        }
    }

    private static class HOLDER {
        private static AESUtil instance = new AESUtil();
    }

    public static AESUtil getInstance() {
        return HOLDER.instance;
    }

    private AESUtil() {
        try {
            iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            skeySpec = new SecretKeySpec(keyInfo.getBytes("UTF-8"), "AES");
            // 这是CBC模式
            // cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            // 默认就是ECB模式
            cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String value) {
        try {
            // CBC模式需要传入向量，ECB模式不需要
            // cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decrypt(String encrypted) {
        try {
            // CBC模式需要传入向量，ECB模式不需要
            // cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));
            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}