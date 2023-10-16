package com.denote.client.utils;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class CryptoUtil {

    public static Key DEFAULT_KEY = null;

    public static final String DEFAULT_SECRET_KEY1 = "sdkkk129s9d9fd0qwer";
    public static final String DEFAULT_SECRET_KEY = DEFAULT_SECRET_KEY1;

    public static final String DES = "DES";

    public static final Base32 base32 = new Base32();

    static {
        DEFAULT_KEY = obtainKey(DEFAULT_SECRET_KEY);
    }

    /**
     * 获得key
     **/
    public static Key obtainKey(String key) {
        if (key == null) {
            return DEFAULT_KEY;
        }
        KeyGenerator generator = null;
        try {
            generator = KeyGenerator.getInstance(DES);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            generator.init(new SecureRandom(key.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Key key1 = generator.generateKey();
        generator = null;
        return key1;
    }

    /**
     * 加密<br>
     * String明文输入,String密文输出
     */
    public static String e(String str) {
        SafeDefine safeDefine = new SafeDefine().invoke();
        byte[] salt = safeDefine.getSalt();
        Key key = safeDefine.getKey();
        SafeSpec safeSpec = new SafeSpec(salt).invoke();
        PBEParameterSpec pbeParameterSpec = safeSpec.getPbeParameterSpec();
        Cipher cipher = safeSpec.getCipher();

        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, pbeParameterSpec);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        byte[] result = new byte[0];
        try {
            result = cipher.doFinal(str.getBytes());
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        String s1 = Base64.encodeBase64String(result);
        System.out.println("jdk PBE encrypt: " + s1);
        return s1;

//        if (true) {
//            return "";
//        }
//        String encodekey = SysConstants.ENCODEKEY;
////        MailUtils.sendMailWithNoSettle("encode detail", "EncodeKey: " + encodekey + ", str: " + str);
//        String s = null;
//        try {
//            byte[] bytes = str.getBytes("UTF-8");
//            System.out.println("encode arr: " + Arrays.toString(bytes));
//            s = Base64.encodeBase64URLSafeString(obtainEncode(encodekey, bytes));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return s;
    }

    /**
     * 加密<br>
     * String明文输入,String密文输出
     */
//    public static String encode64(String key, String str) {
//        try {
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * 解密<br>
     * 以String密文输入,String明文输出
     */
    public static String d(String str) {
        System.out.println("Start Decode: $$" + str + "$$");
        SafeDefine safeDefine = new SafeDefine().invoke();
        byte[] salt = safeDefine.getSalt();
        Key key = safeDefine.getKey();

        SafeSpec safeSpec = new SafeSpec(salt).invoke();
        PBEParameterSpec pbeParameterSpec = safeSpec.getPbeParameterSpec();
        Cipher cipher = safeSpec.getCipher();

        //解密
        //初始化
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, pbeParameterSpec);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        byte[] result = Base64.decodeBase64(str);
        try {
            result = cipher.doFinal(result);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        String s = new String(result);
        System.out.println("jdk PBE decrypt: " + s);
        return s;
//        if (true) {
//            return null;
//        }
//        String key = SysConstants.ENCODEKEY;
//        byte[] str1 = Base64.decodeBase64(str);
//        return new String(obtainDecode(key, str1));
    }

    /**
     * 解密<br>
     * 以String密文输入,String明文输出
     */
//    public static String decode64(String key, String str) {
//
//    }

    /**
     * 加密<br>
     * 以byte[]明文输入,byte[]密文输出
     */
    private static byte[] obtainEncode(String key, byte[] str) {
        byte[] byteFina = null;
        Cipher cipher;
        try {
            Key key1 = obtainKey(key);
            cipher = Cipher.getInstance(DES);
            cipher.init(Cipher.ENCRYPT_MODE, key1);
            byteFina = cipher.doFinal(str);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cipher = null;
        }
        return byteFina;
    }

    /**
     * 解密<br>
     * 以byte[]密文输入,以byte[]明文输出
     */
    private static byte[] obtainDecode(String key, byte[] str) {
        Cipher cipher;
        byte[] byteFina = null;
        try {
            Key key1 = obtainKey(key);
            cipher = Cipher.getInstance(DES);
            cipher.init(Cipher.DECRYPT_MODE, key1);
            byteFina = cipher.doFinal(str);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cipher = null;
        }
        return byteFina;
    }

    public static String e_URL(String s) {
        return URLEncoder.encode(e(s));
    }

    public static String d_URL(String s) {
        return d(URLDecoder.decode((s)));
    }

    private static class SafeDefine {
        private byte[] salt;
        private Key key;

        public byte[] getSalt() {
            return salt;
        }

        public Key getKey() {
            return key;
        }

        public SafeDefine invoke() {
            //初始化盐
            salt = new byte[]{
                    -21, -91, -35, -111, -61, 107, 120, -40
            };
            String defaultCharsetName = Charset.defaultCharset().toString();
            System.out.println("defaultCharsetName: " + defaultCharsetName);
            System.out.println("Salt: " + Arrays.toString(salt));
            /**
             Salt: [-21, -91, -35, -111, -61, 107, 120, -40]
             jdk PBE encrypt: TXn2GyCwNfEiza3evWlZ9Q==
             jdk PBE decrypt: hello,world
             */
            // 加 密 口令与密钥
            String password = DEFAULT_SECRET_KEY1;
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
            SecretKeyFactory factory = null;
            try {
                factory = SecretKeyFactory.getInstance("PBEWITHMD5andDES");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            key = null;
            try {
                key = factory.generateSecret(pbeKeySpec);
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
            return this;
        }
    }

    private static class SafeSpec {
        private byte[] salt;
        private PBEParameterSpec pbeParameterSpec;
        private Cipher cipher;

        public SafeSpec(byte... salt) {this.salt = salt;}

        public PBEParameterSpec getPbeParameterSpec() {
            return pbeParameterSpec;
        }

        public Cipher getCipher() {
            return cipher;
        }

        public SafeSpec invoke() {
            //加密
            pbeParameterSpec = new PBEParameterSpec(salt, 100);
            cipher = null;
            try {
                cipher = Cipher.getInstance("PBEWITHMD5andDES");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
            return this;
        }
    }
}
