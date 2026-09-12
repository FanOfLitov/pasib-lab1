package ru.vlsu.pasib.lab1.cert;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class UserCertificate implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int version;             // V  - Версия формата
    private final long serialNumber;       // SN - Порядковый номер
    private final String algorithmId;      // AI - Идентификатор алгоритма (SHA256withRSA)
    private final String issuerCA;         // CA - Имя удостоверяющего центра
    private final String subjectName;      // A  - Имя владельца сертификата (логин)
    private final String publicKeyBase64;  // Ap - Открытый ключ владельца в Base64
    private final long validFrom;          // TA (начало срока действия)
    private final long validTo;            // TA (окончание срока действия)
    private String signatureBase64;        // Цифровая подпись УЦ

    public UserCertificate(int version,
                           long serialNumber,
                           String algorithmId,
                           String issuerCA,
                           String subjectName,
                           String publicKeyBase64,
                           long validFrom,
                           long validTo) {
        this.version = version;
        this.serialNumber = serialNumber;
        this.algorithmId = algorithmId;
        this.issuerCA = issuerCA;
        this.subjectName = subjectName;
        this.publicKeyBase64 = publicKeyBase64;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public byte[] getBytesToSign() {
        String data = version + "|"
                + serialNumber + "|"
                + algorithmId + "|"
                + issuerCA + "|"
                + subjectName + "|"
                + publicKeyBase64 + "|"
                + validFrom + "|"
                + validTo;
        return data.getBytes(StandardCharsets.UTF_8);
    }

    public boolean isExpired() {
        long now = System.currentTimeMillis();
        return now < validFrom || now > validTo;
    }

    // Геттеры и сеттеры
    public int getVersion() { return version; }
    public long getSerialNumber() { return serialNumber; }
    public String getAlgorithmId() { return algorithmId; }
    public String getIssuerCA() { return issuerCA; }
    public String getSubjectName() { return subjectName; }
    public String getPublicKeyBase64() { return publicKeyBase64; }
    public long getValidFrom() { return validFrom; }
    public long getValidTo() { return validTo; }
    public String getSignatureBase64() { return signatureBase64; }
    public void setSignatureBase64(String signatureBase64) { this.signatureBase64 = signatureBase64; }

    @Override
    public String toString() {
        return "Сертификат [SN: " + serialNumber
                + ", Владелец: " + subjectName
                + ", УЦ: " + issuerCA
                + ", Действителен до: " + new Date(validTo) + "]";
    }
}


