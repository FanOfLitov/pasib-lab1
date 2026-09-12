package ru.vlsu.pasib.lab1.ui;

import ru.vlsu.pasib.lab1.cert.CertificateAuthority;
import ru.vlsu.pasib.lab1.cert.UserCertificate;
import ru.vlsu.pasib.lab1.cert.UsbService;
import ru.vlsu.pasib.lab1.log.AuditLog;
import ru.vlsu.pasib.lab1.model.User;
import ru.vlsu.pasib.lab1.store.UserStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;

public class LoginDialog extends JDialog {

    private static final int MAX_FAILED_ATTEMPTS = 3;

    private final UserStore userStore;

    private final JTextField tfUsername = new JTextField(15);
    private final JPasswordField pfPassword = new JPasswordField(15);
    private final JButton btnLogin = new JButton("Войти");
    private final JButton btnUsbLogin = new JButton("🔑 Вход по USB-сертификату");
    private final JButton btnExit = new JButton("Выход");

    private int failedAttempts = 0;
    private User authenticatedUser = null;

    public LoginDialog(Frame parent, UserStore userStore) {
        super(parent, "Вход в систему СРД", true);
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

        pfPassword.setEchoChar('*');

        JPanel rootPanel = new JPanel(new BorderLayout(10, 10));
        rootPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Строка 1: Имя пользователя
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Имя пользователя:"), gbc);
        gbc.gridx = 1;
        formPanel.add(tfUsername, gbc);

        // Строка 2: Пароль
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Пароль:"), gbc);
        gbc.gridx = 1;
        formPanel.add(pfPassword, gbc);

        // Строка 3: Кнопка USB-входа
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(btnUsbLogin, gbc);

        rootPanel.add(formPanel, BorderLayout.CENTER);

        // Панель кнопок внизу
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);

        rootPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        getRootPane().setDefaultButton(btnLogin);

        btnLogin.addActionListener(e -> tryLogin());
        btnUsbLogin.addActionListener(e -> tryUsbLogin());
        btnExit.addActionListener(e -> onExit());

        pack();
        setResizable(false);
        setLocationRelativeTo(getParent());



    }

    private void tryLogin() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите имя пользователя!", "Предупреждение", JOptionPane.WARNING_MESSAGE);
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
                    "Учётная запись '" + user.getUsername() + "' заблокирована администратором!",
                    "Доступ запрещён",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Если для пользователя включен вход ТОЛЬКО по сертификату
        if (user.isUseCertificate()) {
            JOptionPane.showMessageDialog(this,
                    "Для пользователя '" + user.getUsername() + "' установлен вход строго по USB-сертификату!\n"
                            + "Используйте кнопку 'Вход по USB-сертификату'.",
                    "Требуется сертификат",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (user.checkPassword(password)) {
            AuditLog.info("Успешный вход пользователя '" + user.getUsername() +"' (пароль)");
            this.authenticatedUser = user;
            dispose();
        } else {
            failedAttempts++;
            int remaining = MAX_FAILED_ATTEMPTS - failedAttempts;

            if (remaining <= 0) {
                AuditLog.security("НСДИ: 3 неверных пароля для '" + username + "'. Аварийное завершение");
                JOptionPane.showMessageDialog(this,
                        "Превышено максимальное число попыток ввода пароля (3)!\nПрограмма будет завершена.",
                        "НСДИ / Блокировка",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0);
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


    private void tryUsbLogin() {
        String targetDrive = UsbService.getConfiguredDrive();

        try {

            UserCertificate cert = UsbService.readCertificateFromDrive(targetDrive);

            // Проверяем валидность цифровой подписи УЦ и срок действия
            if (!CertificateAuthority.verifyCertificate(cert)) {
                AuditLog.security("Отклонен невалидный/просроченный сертификат с диска " + targetDrive);
                JOptionPane.showMessageDialog(this,
                        "Сертификат на диске " + targetDrive + " недействителен (истёк срок или нарушена ЭЦП УЦ)!",
                        "Ошибка сертификата",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            //  Ищем пользователя в базе
            String username = cert.getSubjectName();
            Optional<User> userOpt = userStore.findByUsername(username);

            if (userOpt.isEmpty()) {
                AuditLog.warn("USB-сертификат: владелец '" + username + "' не найден в базе");
                JOptionPane.showMessageDialog(this,
                        "Владелец сертификата '" + username + "' не найден в локальной базе!",
                        "Ошибка идентификации",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            User user = userOpt.get();

            if (user.isBlocked()) {
                JOptionPane.showMessageDialog(this,
                        "Учётная запись '" + user.getUsername() + "' заблокирована администратором!",
                        "Доступ запрещён",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            AuditLog.info("Успешный вход '" + user.getUsername() + "' по USB-сертификату SN=" + cert.getSerialNumber());
            JOptionPane.showMessageDialog(this,
                    "Успешная аутентификация по USB-сертификату!\n"
                            + "Владелец: " + cert.getSubjectName() + "\n"
                            + "УЦ: " + cert.getIssuerCA() + "\n"
                            + "Серийный номер: " + cert.getSerialNumber(),
                    "Сертификат подтверждён",
                    JOptionPane.INFORMATION_MESSAGE);

            this.authenticatedUser = user;
            dispose();

        } catch (Exception ex) {
            AuditLog.warn("Ошибка чтения usb-сертификата с " + targetDrive + ": "+ ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Не удалось прочитать сертификат с диска [" + targetDrive + "]:\n" + ex.getMessage(),
                    "Ошибка чтения USB",
                    JOptionPane.ERROR_MESSAGE);
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