package ru.vlsu.pasib.lab1;

import ru.vlsu.pasib.lab1.crypto.CryptoService;
import ru.vlsu.pasib.lab1.model.User;
import ru.vlsu.pasib.lab1.store.UserStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        String testDb = "test_users.dat";
        // Удаляем старый тестовый файл, если остался
        Files.deleteIfExists(Paths.get(testDb));

        System.out.println("первый запуск");
        UserStore store = new UserStore(testDb);
        store.loadOrCreate();

        System.out.println("Пользователей в базе: " + store.getAllUsers().size());
        User admin = store.findByUsername("ADMIN").orElseThrow();
        System.out.println("Админ создан: " + admin.getUsername() + ", пустой пароль: " + admin.checkPassword(""));

        // Смотрим, что физически записалось на диск
        String rawFile = Files.readString(Paths.get(testDb));
        System.out.println("\nСырое содержимое файла на диске (зашифровано Base64):");
        System.out.println(rawFile.trim());
        System.out.println("Расшифрованное: " + CryptoService.decrypt(rawFile).trim());

        System.out.println("\n ДОБАВЛЕНИЕ");
        User ivan = store.addUser("ivan");
        store.setMinLength("ivan", 10);
        store.setRestrictionsEnabled("ivan", true);
        store.changePassword("ivan", "Pass123!#");

        User petr = store.addUser("petr");
        store.setBlocked("petr", true);

        System.out.println("Всего пользователей в памяти: " + store.getAllUsers().size());
        for (User u : store.getAllUsers()) {
            System.out.println(" - " + u);
        }

        System.out.println("\nСИМУЛЯЦИЯ ПЕРЕЗАПУСКА");
        UserStore reloadedStore = new UserStore(testDb);
        reloadedStore.loadOrCreate();

        System.out.println("Загружено после перезапуска: " + reloadedStore.getAllUsers().size());
        User reloadedIvan = reloadedStore.findByUsername("ivan").orElseThrow();
        System.out.println("Ivan пароль верен: " + reloadedIvan.checkPassword("Pass123!#"));
        System.out.println("Ivan ограничения: " + reloadedIvan.isRestrictionsEnabled());
        System.out.println("Ivan мин. длина: " + reloadedIvan.getMinLength());

        User reloadedPetr = reloadedStore.findByUsername("petr").orElseThrow();
        System.out.println("Petr заблокирован: " + reloadedPetr.isBlocked());

        // Проверка запрета дубликатов
        try {
            reloadedStore.addUser("IVAN");
            System.err.println("ОШИБКА: дубликат имени пропущен!");
        } catch (IllegalArgumentException e) {
            System.out.println("\nЗащита от дубликатов сработала: " + e.getMessage());
        }

        // Проверка защиты админа от блокировки
        try {
            reloadedStore.setBlocked("ADMIN", true);
            System.err.println("ОШИБКА: админ заблокирован!");
        } catch (IllegalStateException e) {
            System.out.println("Защита админа от блокировки сработала: " + e.getMessage());
        }

        // Удаляем временный тестовый файл
        Files.deleteIfExists(Paths.get(testDb));
        System.out.println("УСПЕШНО ПРОЙДЕНЫ");
    }
}