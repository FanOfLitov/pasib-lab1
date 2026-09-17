package ru.vlsu.pasib.lab1.store;


import ru.vlsu.pasib.lab1.crypto.CryptoService;
import ru.vlsu.pasib.lab1.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;



public class UserStore {

    public static final String DEFAULT_FILE_NAME = "users.dat";


    private final Path filePath;
    private final List<User> users = new ArrayList<>();

    public UserStore(){
        this(DEFAULT_FILE_NAME);
    }
    public UserStore(String fileName){
        this.filePath = Paths.get(fileName);
    }

    public void loadOrCreate() throws IOException{
        users.clear();

        if(!Files.exists(filePath)){
            User admin = User.createAdmin();
            users.add(admin);
            save();
            return;
        }

        String encryptedContent = Files.readString(filePath, StandardCharsets.UTF_8);
        if(encryptedContent.isBlank()){
            User admin = User.createAdmin();
            users.add(admin);
            save();
            return;
        }
        String decryptedContent = CryptoService.decrypt(encryptedContent);

        String[] lines = decryptedContent.split("\\R");
        for(String line : lines){
            String trimmed = line.trim();
            if(!trimmed.isEmpty()){
                users.add(User.fromFileLine(trimmed));
            }
        }
        if(findByUsername(User.ADMIN_NAME).isEmpty()){
            users.add(0, User.createAdmin());
            save();
        }
    }

    public void save() throws IOException{
        StringBuilder sb = new StringBuilder();
        for(User user : users){
            sb.append(user.toFileLine()).append(System.lineSeparator());
        }

        String plainText = sb.toString();
        String encrypted = CryptoService.encrypt(plainText);

        Files.writeString(filePath, encrypted, StandardCharsets.UTF_8);
    }

    public List<User> getAllUsers(){
        return Collections.unmodifiableList(users);
    }

    public Optional<User> findByUsername(String username){
        if(username == null || username.trim().isEmpty()){
            return Optional.empty();
        }
        String normalized = username.trim();

        for (User u :users){
            if(u.getUsername().equalsIgnoreCase(normalized)){
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    public User addUser(String username) throws IOException{
        if(username == null || username.isBlank()){
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        if (findByUsername(username).isPresent()){
            throw new IllegalArgumentException("Пользователь с именем '" + username + "' уже существует");
        }
        User newUser = User.createNew(username.trim());
        users.add(newUser);
        save();
        return newUser;
    }

    public void setBlocked(String username, boolean blocked) throws IOException{
        User user = findByUsername(username).orElseThrow(() ->new IllegalArgumentException("Пользователь '"+ username + "' не найден"));

        if (user.isAdmin() && blocked){
            throw new IllegalStateException("Нельзя заблокировать администратора(ADMIN)");
        }

        user.setBlocked(blocked);
        save();
    }

    public void setRestrictionsEnambled(String username, boolean enabled) throws IOException{
        User user = findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Пользователь '"+username +"' не найден"));
        user.setRestrictionsEnabled(enabled);
        save();

    }

    public void setMinLength(String username, int minLength) throws IOException{
        User user = findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Пользователь '" + username + "' не найден"));
        user.setMinLength(minLength);
        save();
    }

    public void changePassword(String username, String newPlainPassword) throws IOException{
        User user = findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Пользователь '" + username + "' не найден"));
        user.setPassword(newPlainPassword);
        save();

    }
    public void setRestrictionsEnabled(String username, boolean enabled) throws java.io.IOException {
        User user = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь '" + username + "' не найден"));
        user.setRestrictionsEnabled(enabled);
        save();
    }

    public void removeUser(String username) throws IOException {
        User user = findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Пользователь '" + username + "' не найден"));
        if (user.isAdmin()){
            throw new IllegalStateException("Нельзя удалить учетную запись администратора (ADMIN)");
        }

        users.remove(user);
        save();
    }
}
