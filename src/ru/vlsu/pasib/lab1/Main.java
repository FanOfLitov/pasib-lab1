package ru.vlsu.pasib.lab1;

import ru.vlsu.pasib.lab1.model.User;
import ru.vlsu.pasib.lab1.store.UserStore;
import ru.vlsu.pasib.lab1.ui.LoginDialog;
import ru.vlsu.pasib.lab1.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            try {

                UserStore userStore = new UserStore();
                userStore.loadOrCreate();


                LoginDialog loginDialog = new LoginDialog(null, userStore);
                User authenticatedUser = loginDialog.showDialog();


                if (authenticatedUser != null) {
                    MainFrame mainFrame = new MainFrame(authenticatedUser, userStore);
                    mainFrame.setVisible(true);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Критическая ошибка запуска программы:\n" + e.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}