package ru.vlsu.pasib.lab1;

import ru.vlsu.pasib.lab1.model.User;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args){
        User admin = User.createAdmin();
        admin.setRestrictionsEnabled(true);
        admin.setMinLength(10);
        admin.setBlocked(false);

        String line = admin.toFileLine();
        System.out.println("В файл:   " + line);

        User restored = User.fromFileLine(line);
        System.out.println("Из файла: " + restored);

        System.out.println("Совпало ли всё: " + (
                restored.getUsername().equals(admin.getUsername())
                        && restored.getPasswordHash().equals(admin.getPasswordHash())
                        && restored.isBlocked() == admin.isBlocked()
                        && restored.isRestrictionsEnabled() == admin.isRestrictionsEnabled()
                        && restored.getMinLength() == admin.getMinLength()
                        && restored.isUseCertificate() == admin.isUseCertificate()));

        System.out.println("Пустой пароль подходит: " + restored.checkPassword(""));
        System.out.println("Это администратор: " + restored.isAdmin());

        // проверка регистронезависимости имён
        User fake = User.createNew("admin");
        System.out.println("ADMIN и admin — одно лицо: " + admin.equals(fake));
        System.out.println("Хеши совпадают: " + (admin.hashCode() == fake.hashCode()));

        // проверка защиты формата
        try {
            User.createNew("злоумышленник;ADMIN");
        } catch (IllegalArgumentException e) {
            System.out.println("Инъекция отбита: " + e.getMessage());
        }

        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch(Exception e){
            System.err.println("Не удалось системный стиль включить " + e.getMessage());
        }

        SwingUtilities.invokeLater(() ->{
                JOptionPane.showMessageDialog(
                        null,
                        "Проект созданб окружение работает",
                        "ПАСЗИ, лабораторная 1",
                        JOptionPane.INFORMATION_MESSAGE
                );
        });
    }
}