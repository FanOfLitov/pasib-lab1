package ru.vlsu.pasib.lab1.model;

import  java.util.Objects;

public class User {

    private String username;
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
        this.username = username;
        this.passwordHash=passwordHash;
        this.blocked = blocked;
        this.restrictionsEnabled = restrictionsEnabled;
        this.minLength=minLength;
        this.useCertificate=useCertificate;
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
        boolean blocked = Boolean.parseBoolean(parts[3]);
        int minLength = Integer.parseInt(parts[4]);
        boolean useCertificate = Boolean.parseBoolean(parts[5]);
        return  new User(username, passwordHash, blocked, restrictionsEnabled, minLength, useCertificate);
    }
    @Override
    public boolean equals(Object o){
        if(this==o){
            return true;
        }
        if (!(o instanceof User)){
            return false;
        }
        User other = (User) o;
        return Objects.equals(username, other.username);
    }


    @Override
    public int hashCode(){
        return Objects.hash(username);

    }

    @Override
    public String toString(){
        return "User{username'"+username+ '\''
                +", blocked=" + blocked
                +", minLength=" +minLength+'}';
    }
}
