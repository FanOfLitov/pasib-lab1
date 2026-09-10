package ru.vlsu.pasib.lab1.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class CryptoService {

    private static final String APP_SECRET = "PASIB_LAB1_VLSU_BLUMENTHAL_2026_SECRET_KEY";
    private static final String TRANSFORMATION ="AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "AES";

    private static final byte[] IV = new byte[]{
            0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef,
            0x10, 0x32, 0x54, 0x76, (byte) 0x98, (byte) 0xba, (byte) 0xdc, (byte) 0xfe
    };

    private CryptoService(){}

    private static SecretKeySpec deriveKey(){
        try{
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha.digest(APP_SECRET.getBytes(StandardCharsets.UTF_8));
            byte[] keyBytes =Arrays.copyOf(hash, 16);
            return new SecretKeySpec(keyBytes, ALGORITHM);

        }catch (NoSuchAlgorithmException e){
            throw new IllegalStateException("SHA-256 недоступен", e);
        }
    }

    public static String encrypt(String plainText){
        if(plainText == null){
            return null;
        } try {
            SecretKeySpec key = deriveKey();
            IvParameterSpec ivSpec = new IvParameterSpec(IV);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }catch (Exception e){
            throw new RuntimeException("Ошибки шифрования данных: " + e.getMessage(), e);
        }
    }


    public static String decrypt(String cipherTextBase64){
        if(cipherTextBase64 ==null){
            return null;
        }
        try{
            SecretKeySpec key =deriveKey();
            IvParameterSpec ivSpec = new IvParameterSpec(IV);

            Cipher cipher =Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            byte[] cipherBytes = Base64.getDecoder().decode(cipherTextBase64.trim());
            byte[] decryptedBytes = cipher.doFinal(cipherBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch(Exception e){
            throw new RuntimeException("Ошибка расшифровки данных(неверный ключ или файл поврежден", e);
        }
    }
}
