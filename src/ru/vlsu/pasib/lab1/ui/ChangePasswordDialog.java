package ru.vlsu.pasib.lab1.ui;

import ru.vlsu.pasib.lab1.model.User;
import ru.vlsu.pasib.lab1.security.PasswordPolicy;
import ru.vlsu.pasib.lab1.security.PasswordPolicy.CheckResult;
import ru.vlsu.pasib.lab1.store.UserStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class ChangePasswordDialog extends JDialog {

    private final User user;
    private final UserStore userStore;

    private final JPasswordField pfOldPassword = new JPasswordField(15);
    private final JPasswordField pfNewPassword = new JPasswordField(15);
    private final JPasswordField pfConfirmPassword = new JPasswordField(15);

    private final JButton btnChange = new JButton("Сменить пароль");
    private final JButton btnCancel = new JButton("Отмена");

    private boolean success = false;

    public ChangePasswordDialog(Frame parent, User user, UserStore userStore) {
        super(parent, "Смена пароля: " + user.getUsername(), true);
        this.user = user;
        this.userStore = userStore;

        initUI();
    }

    private void initUI() {
        pfOldPassword.setEchoChar('*');
        pfNewPassword.setEchoChar('*');
        pfConfirmPassword.setEchoChar('*');

        JPanel rootPanel = new JPanel(new BorderLayout(10, 10));
        rootPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Старый пароль
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Старый пароль:"), gbc);
        gbc.gridx = 1;
        formPanel.add(pfOldPassword, gbc);

        // 2. Новый пароль
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Новый пароль:"), gbc);
        gbc.gridx = 1;
        formPanel.add(pfNewPassword, gbc);

        // 3. Подтверждение
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Подтверждение:"), gbc);
        gbc.gridx = 1;
        formPanel.add(pfConfirmPassword, gbc);

        // Подсказка о требованиях к паролю
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        String reqInfo = user.isRestrictionsEnabled()
                ? "<html><small style='color: gray;'>Ограничения включены (мин. длина: " + user.getMinLength() + ")</small></html>"
                : "<html><small style='color: gray;'>Ограничения сложности отключены</small></html>";
        formPanel.add(new JLabel(reqInfo), gbc);

        rootPanel.add(formPanel, BorderLayout.CENTER);

        // Панель кнопок
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.add(btnChange);
        btnPanel.add(btnCancel);
        rootPanel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        getRootPane().setDefaultButton(btnChange);

        btnChange.addActionListener(e -> tryChangePassword());
        btnCancel.addActionListener(e -> dispose());

        pack();
        setResizable(false);
        setLocationRelativeTo(getParent());
    }

    private void tryChangePassword() {
        String oldPass = new String(pfOldPassword.getPassword());
        String newPass = new String(pfNewPassword.getPassword());
        String confirmPass = new String(pfConfirmPassword.getPassword());

        // 1. Проверка старого пароля
        if (!user.checkPassword(oldPass)) {
            JOptionPane.showMessageDialog(this,
                    "Неверно указан старый пароль!",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            pfOldPassword.setText("");
            pfOldPassword.requestFocus();
            return;
        }

        // 2. Проверка совпадения нового пароля и подтверждения
        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this,
                    "Новый пароль и подтверждение не совпадают!",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            pfConfirmPassword.setText("");
            pfConfirmPassword.requestFocus();
            return;
        }

        // 3. Проверка сложности пароля по PasswordPolicy (если включены ограничения)
        if (user.isRestrictionsEnabled()) {
            CheckResult checkResult = PasswordPolicy.check(newPass, user.getMinLength());
            if (!checkResult.allPassed()) {
                // Выводим детальный отчёт по каждому невыполненному требованию (требование методички!)
                JOptionPane.showMessageDialog(this,
                        "Новый пароль не удовлетворяет требованиям сложности:\n\n"
                                + checkResult.toReport(),
                        "Требования к паролю не выполнены",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // 4. Сохранение нового пароля
        try {
            userStore.changePassword(user.getUsername(), newPass);
            this.success = true;
            JOptionPane.showMessageDialog(this,
                    "Пароль успешно изменён!",
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при сохранении пароля: " + ex.getMessage(),
                    "Ошибка ввода/вывода",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() {
        return success;
    }
}