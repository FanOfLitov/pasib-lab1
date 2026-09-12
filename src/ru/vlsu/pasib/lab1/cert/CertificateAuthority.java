package ru.vlsu.pasib.lab1.cert;

import java.security.*;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;

public final class CertificateAuthority {

    public static final String CA_NAME = "ВлГУ УЦ ПАСЗИ (Кафедра ИЗИ)";
    public static final String SIGN_ALGORITHM = "SHA256withRSA";

    private static final KeyPair caKeyPair;
    private static final AtomicLong serialCounter = new AtomicLong(1000);

    static {
        // При старте приложения генерируем мастер-ключи УЦ (RSA 2048 бит)
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            caKeyPair = keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Алгоритм RSA недоступен", e);
        }
    }

    private CertificateAuthority() {
    }

    public static UserCertificate issueCertificate(String username, int validityDays) {
        try {
            // Генерируем персональную ключевую пару пользователя
            KeyPairGenerator userKeyGen = KeyPairGenerator.getInstance("RSA");
            userKeyGen.initialize(1024);
            KeyPair userPair = userKeyGen.generateKeyPair();

            String userPubKeyBase64 = Base64.getEncoder().encodeToString(userPair.getPublic().getEncoded());

            long now = System.currentTimeMillis();
            long validUntil = now + (long) validityDays * 24 * 60 * 60 * 1000;

            //Формируем тело сертификата по формуле
            UserCertificate cert = new UserCertificate(
                    1,
                    serialCounter.incrementAndGet(),
                    SIGN_ALGORITHM,
                    CA_NAME,
                    username,
                    userPubKeyBase64,
                    now,
                    validUntil
            );

            // УЦ подписывает сертификат своим закрытым ключом
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

        //  Проверка срока действия
        if (cert.isExpired()) {
            return false;
        }

        //  Проверка цифровой подписи открытым ключом УЦ
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
