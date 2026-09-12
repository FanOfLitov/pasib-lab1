package ru.vlsu.pasib.lab1;

import ru.vlsu.pasib.lab1.log.AuditLog;
import ru.vlsu.pasib.lab1.model.User;
import ru.vlsu.pasib.lab1.store.UserStore;
import ru.vlsu.pasib.lab1.ui.LoginDialog;
import ru.vlsu.pasib.lab1.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Хук на аварийное/нормальное завершение JVM
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            AuditLog.info("=== Завершение работы СРД ===");
        }));

        SwingUtilities.invokeLater(() -> {
            try {
                AuditLog.info("=== Запуск СРД ===");

                UserStore userStore = new UserStore();
                userStore.loadOrCreate();

                LoginDialog loginDialog = new LoginDialog(null, userStore);
                User user = loginDialog.showDialog();

                if (user != null) {
                    AuditLog.info("Сеанс открыт: " + user.getUsername()
                            + (user.isAdmin() ? " [ADMIN]" : " [USER]"));
                    MainFrame frame = new MainFrame(user, userStore);
                    frame.setVisible(true);
                } else {
                    AuditLog.info("Вход не выполнен, выход из программы");
                }
            } catch (Exception e) {
                AuditLog.security("Критическая ошибка: " + e.getMessage());
                JOptionPane.showMessageDialog(null,
                        "Критическая ошибка:\n" + e.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}