package ru.vlsu.pasib.lab1.ui;

import ru.vlsu.pasib.lab1.model.User;
import ru.vlsu.pasib.lab1.store.UserStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;

/**
 * Диалоговое окно авторизации пользователя (п. 9, 10, 11 требований).
 */
public class LoginDialog extends JDialog {

    private static final int MAX_FAILED_ATTEMPTS = 3;

    private final UserStore userStore;

    private final JTextField tfUsername = new JTextField(15);
    private final JPasswordField pfPassword = new JPasswordField(15);
    private final JButton btnLogin = new JButton("Войти");
    private final JButton btnExit = new JButton("Выход");

    private int failedAttempts = 0;
    private User authenticatedUser = null;

    public LoginDialog(Frame parent, UserStore userStore) {
        super(parent, "Вход в систему СРД", true); // true = модальный диалог
        this.userStore = userStore;

        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });

        // имволы маскируются звездочкой '*' (п. 9)
        pfPassword.setEchoChar('*');

        // Основная панель с отступами
        JPanel rootPanel = new JPanel(new BorderLayout(10, 10));
        rootPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Панель формы (сетка 2 строки, 2 колонки)
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Строка 1: Имя пользователя
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Имя пользователя:"), gbc);

        gbc.gridx = 1;
        formPanel.add(tfUsername, gbc);


        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Пароль:"), gbc);

        gbc.gridx = 1;
        formPanel.add(pfPassword, gbc);

        rootPanel.add(formPanel, BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);

        rootPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);


        getRootPane().setDefaultButton(btnLogin);


        btnLogin.addActionListener(e -> tryLogin());
        btnExit.addActionListener(e -> onExit());

        pack();
        setResizable(false);
        setLocationRelativeTo(getParent());
    }

    private void tryLogin() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Введите имя пользователя!",
                    "Предупреждение",
                    JOptionPane.WARNING_MESSAGE);
            tfUsername.requestFocus();
            return;
        }

        Optional<User> userOpt = userStore.findByUsername(username);


        if (userOpt.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Пользователь с именем '" + username + "' не найден.\nПовторить ввод?",
                    "Ошибка идентификации",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                tfUsername.selectAll();
                tfUsername.requestFocus();
            } else {
                onExit();
            }
            return;
        }

        User user = userOpt.get();

         if (user.isBlocked()) {
            JOptionPane.showMessageDialog(this,
                    "Учётная запись '" + user.getUsername() + "' заблокирована администратором системы!",
                    "Доступ запрещён",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }


        if (user.checkPassword(password)) {

            this.authenticatedUser = user;
            dispose();
        } else {

            failedAttempts++;
            int remaining = MAX_FAILED_ATTEMPTS - failedAttempts;

            if (remaining <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Превышено максимальное число попыток ввода пароля (3)!\nПрограмма будет завершена.",
                        "НСДИ / Блокировка",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0); // Завершение работы программы (п. 11)
            } else {
                JOptionPane.showMessageDialog(this,
                        "Неверный пароль!\nОсталось попыток: " + remaining,
                        "Ошибка аутентификации",
                        JOptionPane.WARNING_MESSAGE);
                pfPassword.setText("");
                pfPassword.requestFocus();
            }
        }
    }

    private void onExit() {
        dispose();
        System.exit(0);
    }


    public User showDialog() {
        setVisible(true);
        return authenticatedUser;
    }
}