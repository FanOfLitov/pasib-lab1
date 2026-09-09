package ru.vlsu.pasib.lab1.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import  java.util.Objects;

public class User {

    public static final String ADMIN_NAME ="ADMIN";
    public static final int DEFAULT_MIN_LENGTH=8;
    private static final String SEPARATOR =":";
    private static final int FIELD_COUNT=6;

    private final String username;
    private String passwordHash;
    private boolean blocked;
    private boolean restrictionsEnabled;
    private int minLength;
    private boolean useCertificate;

    public User(String username,
                String passwordHash,
                boolean blocked,
                boolean restrictionsEnabled,
                int minLength,
                boolean useCertificate){
        if (username == null || username.isBlank()){
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        if (minLength<0){
            throw new IllegalArgumentException("Минимальная длина не может быть отрицательной");
        }
        if (username.contains(SEPARATOR) || username.contains("\n") || username.contains("\r")) {
            throw new IllegalArgumentException(
                    "Имя пользователя не должно содержать '" + SEPARATOR + "' и переводов строки");
        }
        if (passwordHash == null) {
            throw new IllegalArgumentException("Хеш пароля не может быть null");
        }
        if (minLength < 0) {
            throw new IllegalArgumentException("Минимальная длина не может быть отрицательной");
        }

        this.username = username;
        this.passwordHash=passwordHash;
        this.blocked = blocked;
        this.restrictionsEnabled = restrictionsEnabled;
        this.minLength=minLength;
        this.useCertificate=useCertificate;
    }

    public static User createNew(String username) {
        return new User(username, PasswordHasher.hash(""),
                false, false, DEFAULT_MIN_LENGTH, false);
    }


    public static User createAdmin() {
        return new User(ADMIN_NAME, PasswordHasher.hash(""),
                false, false, DEFAULT_MIN_LENGTH, false);
    }

    public String getUsername(){
        return username;
    }

    public String getPasswordHash(){
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash){
        this.passwordHash=passwordHash;
    }
    public boolean isBlocked(){
        return blocked;
    }
    public void setBlocked(boolean blocked){

        this.blocked=blocked;
    }

    public boolean isRestrictionsEnabled(){
        return  restrictionsEnabled;
    }
    public void setRestrictionsEnabled(boolean restrictionsEnabled){
        this.restrictionsEnabled=restrictionsEnabled;
    }
    public int getMinLength(){
        return minLength;
    }

    public void setMinLength(int minLength){
        if (minLength<0) {
            throw new IllegalArgumentException("Минимальная длина не может быть отрицательной");
        }
        this.minLength=minLength;

    }

    public boolean isUseCertificate(){
        return useCertificate;
    }

    public void setUseCertificate(boolean useCertificate){
        this.useCertificate = useCertificate;
    }

    public String toFileLine(){
        return username + ";"+passwordHash + ";"
                +blocked + ";" + restrictionsEnabled + ";"
                +minLength + ";" +useCertificate;
    }

    public static User fromFileLine(String line){
        String[] parts = line.split(";", -1);
        if(parts.length !=6){
            throw new IllegalArgumentException("Битый файл пользователей: "+ line);

        }
        String username = parts[0];
        String passwordHash = parts[1];
        boolean blocked = Boolean.parseBoolean(parts[2]);
        int minLength = Integer.parseInt(parts[4]);
        boolean useCertificate = Boolean.parseBoolean(parts[5]);
        return  new User(username, passwordHash, blocked, true, minLength, useCertificate);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return username.equalsIgnoreCase(other.username);
    }

    @Override
    public int hashCode() {
        return username.toLowerCase(Locale.ROOT).hashCode();
    }



    @Override
    public String toString(){
        return "User{username = '"+username+ '\''
                +", blocked=" + blocked
                +", minLength=" +minLength+'}';
    }

    public final class PasswordHasher {

        private PasswordHasher() {
        }

        public static String hash(String plainPassword) {
            if (plainPassword == null) {
                plainPassword = "";
            }
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] bytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder(bytes.length * 2);
                for (byte b : bytes) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Алгоритм SHA-256 недоступен в этой JVM", e);
            }
        }

        public static boolean matches(String plainPassword, String expectedHash) {
            return hash(plainPassword).equals(expectedHash);
        }
    }



    public boolean checkPassword(String plainPassword) {
        return PasswordHasher.matches(plainPassword, passwordHash);
    }

    public void setPassword(String plainPassword) {
        this.passwordHash = PasswordHasher.hash(plainPassword);
    }

    public boolean isAdmin() {
        return ADMIN_NAME.equalsIgnoreCase(username);
    }

}

