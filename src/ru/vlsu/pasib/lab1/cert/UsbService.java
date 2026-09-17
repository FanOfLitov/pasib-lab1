package ru.vlsu.pasib.lab1.cert;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;


public final class UsbService {

    public static final String CERT_FILE_NAME = "auth.cert";
    private static final String CONFIG_FILE = "config.properties";
    private static final String PROP_TARGET_DRIVE = "target.usb.drive";

    private UsbService() {
    }



    public static String normalizeDrive(String drivePath) {
        if (drivePath == null || drivePath.isBlank()) {
            throw new IllegalArgumentException("Путь к диску пустой");
        }
        String d = drivePath.trim().replace('/', '\\');


        if (d.length() == 1 && Character.isLetter(d.charAt(0))) {
            return Character.toUpperCase(d.charAt(0)) + ":\\";
        }
        if (d.length() >= 2 && Character.isLetter(d.charAt(0)) && d.charAt(1) == ':') {
            char letter = Character.toUpperCase(d.charAt(0));
            if (d.length() == 2) {
                return letter + ":\\";
            }

            String rest = d.substring(2);
            if (rest.startsWith("\\")) {
                rest = rest.substring(1);
            }
            if (rest.isEmpty()) {
                return letter + ":\\";
            }
            return letter + ":\\" + rest;
        }
        return d;
    }

    public static List<File> getAvailableDrives() {
        File[] roots = File.listRoots();
        if (roots == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(roots);
    }


    public static String getConfiguredDrive() {
        Path cfg = Paths.get(CONFIG_FILE).toAbsolutePath();
        Properties props = new Properties();
        if (Files.exists(cfg)) {
            try (InputStream in = Files.newInputStream(cfg)) {
                props.load(in);
                String drive = props.getProperty(PROP_TARGET_DRIVE);
                if (drive != null && !drive.isBlank()) {
                    return normalizeDrive(drive);
                }
            } catch (IOException e) {
                System.err.println("Не удалось прочитать " + cfg + ": " + e.getMessage());
            }
        }
        return null;
    }


    public static String getConfigLocation() {
        return Paths.get(CONFIG_FILE).toAbsolutePath().toString();
    }

    public static void saveConfiguredDrive(String drivePath) throws IOException {
        String normalized = normalizeDrive(drivePath);
        Path cfg = Paths.get(CONFIG_FILE).toAbsolutePath();

        Properties props = new Properties();
        if (Files.exists(cfg)) {
            try (InputStream in = Files.newInputStream(cfg)) {
                props.load(in);
            }
        }
        props.setProperty(PROP_TARGET_DRIVE, normalized);

        try (OutputStream out = Files.newOutputStream(cfg)) {
            props.store(out, "PASIB Lab1 USB drive settings");
        }

        System.out.println("USB-диск сохранён: " + normalized + " → файл " + cfg);
    }

    public static void writeCertificateToDrive(String drivePath, UserCertificate cert) throws IOException {
        String drive = normalizeDrive(drivePath);
        Path targetPath = Paths.get(drive, CERT_FILE_NAME);
        Files.createDirectories(targetPath.getParent() != null ? targetPath.getParent() : Paths.get(drive));
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(targetPath))) {
            oos.writeObject(cert);
        }
        System.out.println("Сертификат записан: " + targetPath.toAbsolutePath());
    }

    public static UserCertificate readCertificateFromDrive(String drivePath)
            throws IOException, ClassNotFoundException {
        if (drivePath == null || drivePath.isBlank()) {
            throw new IOException("USB-диск не настроен. Войдите как ADMIN и укажите диск (например E:\\)");
        }
        String drive = normalizeDrive(drivePath);
        Path certPath = Paths.get(drive, CERT_FILE_NAME).toAbsolutePath();
        if (!Files.exists(certPath)) {
            throw new FileNotFoundException(
                    "Файл '" + CERT_FILE_NAME + "' не найден по пути:\n"
                            + certPath + "\n\n"
                            + "Проверьте:\n"
                            + "1) флешка вставлена и буква верна;\n"
                            + "2) сертификат выпускали именно на этот диск.");
        }
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(certPath))) {
            return (UserCertificate) ois.readObject();
        }
    }


    public static String certFileNameFor(String username){
        String safe = username.trim().replaceAll("[^a-zA-Z0-9._\\-А-Яа-яЁё]", "_");
        return "auth_" + safe+ ".cert";
    }


    public static void writeCertificateToDrive(String drivePath, String username, UserCertificate cert) throws IOException{
        String drive = normalizeDrive(drivePath);
        Path targetPath = Paths.get(drive, certFileNameFor(username));
        Path parent = targetPath.getParent();
        if (parent!=null){
            Files.createDirectories(parent);
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(targetPath))){
            oos.writeObject(cert);
        }
        Path legacy = Paths.get(drive, CERT_FILE_NAME);
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(legacy))){
            oos.writeObject(cert);
        }
        System.out.println("Сертификат записан: " + targetPath.toAbsolutePath());
    }

    public static UserCertificate readCertificateFromDrive(String drivePath, String usernameHint)
            throws IOException, ClassNotFoundException {

        if (drivePath == null || drivePath.isBlank()) {
            throw new IOException("USB-диск не настроен (ADMIN → Настройка USB-диска → E:\\)");
        }
        String drive = normalizeDrive(drivePath);
        Path dir = Paths.get(drive);

        if (!Files.isDirectory(dir)) {
            throw new FileNotFoundException("Диск/папка недоступны: " + dir.toAbsolutePath());
        }

        Path certPath = null;

        if (usernameHint != null && !usernameHint.isBlank()) {
            Path named = dir.resolve(certFileNameFor(usernameHint));
            if (Files.exists(named)) {
                certPath = named;
            }
        }

        if (certPath == null) {
            try (var stream = Files.list(dir)) {
                List<Path> certs = stream
                        .filter(p -> {
                            String n = p.getFileName().toString().toLowerCase();
                            return n.startsWith("auth_") && n.endsWith(".cert");
                        })
                        .sorted()
                        .toList();

                if (certs.size() == 1) {
                    certPath = certs.get(0);
                } else if (certs.size() > 1) {
                    throw new IOException(
                            "На диске несколько сертификатов (" + certs.size() + ").\n"
                                    + "Введите имя пользователя в поле входа — будет выбран auth_<имя>.cert\n"
                                    + "или оставьте на флешке сертификат только одного пользователя.");
                }
            }
        }

        if (certPath == null) {
            Path legacy = dir.resolve(CERT_FILE_NAME);
            if (Files.exists(legacy)) {
                certPath = legacy;
            }
        }

        if (certPath == null) {
            throw new FileNotFoundException(
                    "На " + drive + " нет файлов auth_<user>.cert / auth.cert");
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(certPath))) {
            Object obj = ois.readObject();
            if (!(obj instanceof UserCertificate)) {
                throw new IOException("Файл " + certPath.getFileName() + " не является сертификатом СРД");
            }
            return (UserCertificate) obj;
        } catch (StreamCorruptedException | InvalidClassException e) {
            throw new IOException("Файл сертификата повреждён или от другой версии программы:\n"
                    + certPath.toAbsolutePath() + "\nВыпустите ключ заново.", e);
        }
    }
}