package ru.vlsu.pasib.lab1.cert;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;


public final class CertificateAuthority {

    public static final String CA_NAME = "ВлГУ УЦ ПАСЗИ (Кафедра ИЗИ)";
    public static final String SIGN_ALGORITHM = "SHA256withRSA";

    private static final Path KEYSTORE_FILE = Paths.get("ca_keystore.dat");
    private static final KeyPair caKeyPair;
    private static final AtomicLong serialCounter = new AtomicLong(1000);

    static {
        try {
            caKeyPair = loadOrCreateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось инициализировать ключи УЦ", e);
        }
    }

    private CertificateAuthority() {
    }

    private static KeyPair loadOrCreateKeyPair() throws Exception {
        if (Files.exists(KEYSTORE_FILE)) {

            String content = Files.readString(KEYSTORE_FILE).trim();
            String[] parts = content.split("\\R");
            if (parts.length >= 2) {
                byte[] pubBytes = Base64.getDecoder().decode(parts[0].trim());
                byte[] privBytes = Base64.getDecoder().decode(parts[1].trim());

                KeyFactory kf = KeyFactory.getInstance("RSA");
                PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(pubBytes));
                PrivateKey priv = kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));

                if (parts.length >= 3) {
                    try {
                        serialCounter.set(Long.parseLong(parts[2].trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                return new KeyPair(pub, priv);
            }
        }


        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        saveKeyPair(pair);
        return pair;
    }

    private static void saveKeyPair(KeyPair pair) throws IOException {
        String pub = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        String priv = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
        String serial = String.valueOf(serialCounter.get());
        Files.writeString(KEYSTORE_FILE, pub + "\n" + priv + "\n" + serial);
    }

    private static void saveSerial() {
        try {
            if (!Files.exists(KEYSTORE_FILE)) {
                return;
            }
            String content = Files.readString(KEYSTORE_FILE).trim();
            String[] parts = content.split("\\R");
            if (parts.length >= 2) {
                Files.writeString(KEYSTORE_FILE,
                        parts[0].trim() + "\n" + parts[1].trim() + "\n" + serialCounter.get());
            }
        } catch (IOException e) {
            System.err.println("Не удалось сохранить серийный номер УЦ: " + e.getMessage());
        }
    }

    public static UserCertificate issueCertificate(String username, int validityDays) {
        try {
            KeyPairGenerator userKeyGen = KeyPairGenerator.getInstance("RSA");
            userKeyGen.initialize(2048);
            KeyPair userPair = userKeyGen.generateKeyPair();

            String userPubKeyBase64 = Base64.getEncoder()
                    .encodeToString(userPair.getPublic().getEncoded());

            long now = System.currentTimeMillis();
            long validUntil = now + (long) validityDays * 24L * 60L * 60L * 1000L;

            long sn = serialCounter.incrementAndGet();
            saveSerial();

            UserCertificate cert = new UserCertificate(
                    1,
                    sn,
                    SIGN_ALGORITHM,
                    CA_NAME,
                    username,
                    userPubKeyBase64,
                    now,
                    validUntil
            );

            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initSign(caKeyPair.getPrivate());
            signature.update(cert.getBytesToSign());
            byte[] sigBytes = signature.sign();
            cert.setSignatureBase64(Base64.getEncoder().encodeToString(sigBytes));

            return cert;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка выпуска сертификата: " + e.getMessage(), e);
        }
    }

    public static boolean verifyCertificate(UserCertificate cert) {
        if (cert == null || cert.getSignatureBase64() == null) {
            return false;
        }
        if (cert.isExpired()) {
            return false;
        }
        try {
            Signature signature = Signature.getInstance(cert.getAlgorithmId());
            signature.initVerify(caKeyPair.getPublic());
            signature.update(cert.getBytesToSign());
            byte[] sigBytes = Base64.getDecoder().decode(cert.getSignatureBase64());
            return signature.verify(sigBytes);
        } catch (Exception e) {
            return false;
        }
    }
}