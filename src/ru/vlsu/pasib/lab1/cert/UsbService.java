package ru.vlsu.pasib.lab1.cert;


import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class UsbService {

    public static final String CERT_FILE_NAME = "auth.cert";
    private static final String CONFIG_FILE = "config.properties";
    private static final String PROP_TARGET_DRIVE = "target.usb.drive";

    private UsbService() {
    }
    public static List<File> getAvailableDrives() {
        File[] roots = File.listRoots();
        if (roots == null) return Collections.emptyList();
        return Arrays.asList(roots);
    }
    public static String getConfiguredDrive() {
        Properties props = new Properties();
        if (Files.exists(Paths.get(CONFIG_FILE))) {
            try (InputStream in = Files.newInputStream(Paths.get(CONFIG_FILE))) {
                props.load(in);
                String drive = props.getProperty(PROP_TARGET_DRIVE);
                if (drive != null && !drive.isBlank()) {
                    return drive.trim();
                }
            } catch (IOException ignored) {
            }
        }
        FileSystemView fsv = FileSystemView.getFileSystemView();
        for (File root : getAvailableDrives()) {
            if (fsv.isDrive(root) && !root.getAbsolutePath().startsWith("C")) {
                return root.getAbsolutePath();
            }
        }
        return "E:\\"; // Значение по умолчанию
    }

    public static void saveConfiguredDrive(String drivePath) throws IOException {
        Properties props = new Properties();
        if (Files.exists(Paths.get(CONFIG_FILE))) {
            try (InputStream in = Files.newInputStream(Paths.get(CONFIG_FILE))) {
                props.load(in);
            }
        }
        props.setProperty(PROP_TARGET_DRIVE, drivePath);
        try (OutputStream out = Files.newOutputStream(Paths.get(CONFIG_FILE))) {
            props.store(out, "PASIB Lab 1 Configuration Settings");
        }
    }

    public static void writeCertificateToDrive(String drivePath, UserCertificate cert) throws IOException {
        Path targetPath = Paths.get(drivePath, CERT_FILE_NAME);
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(targetPath))) {
            oos.writeObject(cert);
        }
    }

    public static UserCertificate readCertificateFromDrive(String drivePath) throws IOException, ClassNotFoundException {
        Path certPath = Paths.get(drivePath, CERT_FILE_NAME);
        if (!Files.exists(certPath)) {
            throw new FileNotFoundException("Файл сертификата '" + CERT_FILE_NAME + "' не найден на диске " + drivePath);
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(certPath))) {
            return (UserCertificate) ois.readObject();
        }
    }
}







