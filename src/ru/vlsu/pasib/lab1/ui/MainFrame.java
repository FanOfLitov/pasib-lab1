package ru.vlsu.pasib.lab1.ui;

import ru.vlsu.pasib.lab1.log.AuditLog;
import ru.vlsu.pasib.lab1.cert.CertificateAuthority;
import ru.vlsu.pasib.lab1.model.User;
import ru.vlsu.pasib.lab1.store.UserStore;
import ru.vlsu.pasib.lab1.cert.UserCertificate;
import ru.vlsu.pasib.lab1.cert.UsbService;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class MainFrame extends JFrame {

    private final User currentUser;
    private final UserStore userStore;

    private UserTableModel tableModel;
    private JTable usersTable;
    private JLabel lblStatus;


    private JMenu menuAdmin;
    private JButton btnAddUser;
    private JButton btnToggleBlock;
    private JButton btnToggleRestrictions;
    private JButton btnSetMinLength;

    public MainFrame(User currentUser, UserStore userStore) {
        super("Система СРД — [" + currentUser.getUsername() + " : "
                + (currentUser.isAdmin() ? "АДМИНИСТРАТОР" : "ПОЛЬЗОВАТЕЛЬ") + "]");
        this.currentUser = currentUser;
        this.userStore = userStore;

        initUI();
        applyAccessControl(); // Применяем разграничение полномочий (п. 1, 2, 8)
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);


        setJMenuBar(createMenuBar());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(createToolBar(), BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);


        if (currentUser.isAdmin()) {
            tableModel = new UserTableModel(userStore.getAllUsers());
            usersTable = new JTable(tableModel);
            usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            usersTable.setRowHeight(24);
            add(new JScrollPane(usersTable), BorderLayout.CENTER);
        } else {
            JPanel userPanel = new JPanel(new GridBagLayout());
            JLabel lblUserWelcome = new JLabel("<html><center>"
                    + "<h2>Добро пожаловать, " + currentUser.getUsername() + "!</h2>"
                    + "<p>Вы вошли в режиме <b>обычного пользователя</b>.</p>"
                    + "<p style='color: gray;'>Функции администрирования системы ограничены СРД.<br>"
                    + "Вам доступна функция смены пароля в меню «Безопасность».</p>"
                    + "</center></html>");
            userPanel.add(lblUserWelcome);
            add(userPanel, BorderLayout.CENTER);
        }


        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        lblStatus = new JLabel();
        lblStatus.setBorder(new EmptyBorder(4, 8, 4, 8));
        updateStatusBar();
        statusPanel.add(lblStatus, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JMenuBar createMenuBar() {


        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("Файл");
        JMenuItem miChangePassword = new JMenuItem("Сменить пароль...");
        miChangePassword.addActionListener(e -> onChangePassword());

        JMenuItem miExit = new JMenuItem("Выход");
        miExit.addActionListener(e -> onExit());

        menuFile.add(miChangePassword);
        menuFile.addSeparator();
        menuFile.add(miExit);
        menuBar.add(menuFile);


        menuAdmin = new JMenu("Пользователи");
        JMenuItem miAdd = new JMenuItem("Добавить пользователя...");
        miAdd.addActionListener(e -> onAddUser());


        JMenuItem miBlock = new JMenuItem("Блокировать / Разблокировать");
        miBlock.addActionListener(e -> onToggleBlock());

        JMenuItem miRestrictions = new JMenuItem("Вкл / Выкл ограничения пароля");
        miRestrictions.addActionListener(e -> onToggleRestrictions());

        JMenuItem miLength = new JMenuItem("Установить мин. длину пароля...");
        miLength.addActionListener(e -> onSetMinLength());

        menuAdmin.add(miAdd);
        menuAdmin.addSeparator();
        menuAdmin.add(miBlock);
        menuAdmin.add(miRestrictions);
        menuAdmin.add(miLength);
        menuBar.add(menuAdmin);

        JMenu menuHelp = new JMenu("Справка");
        JMenuItem miAbout = new JMenuItem("О программе...");
        miAbout.addActionListener(e -> onAbout());
        menuHelp.add(miAbout);
        menuBar.add(menuHelp);

        JMenuItem miCert = new JMenuItem("Выпустить USB-сертификат...");
        miCert.addActionListener(e -> onIssueCertificate());

        JMenuItem miDrive = new JMenuItem("Настройка USB-диска...");
        miDrive.addActionListener(e -> onConfigureUsbDrive());

        menuAdmin.addSeparator();
        menuAdmin.add(miCert);
        menuAdmin.add(miDrive);

        return menuBar;
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton btnChangePass = new JButton("Сменить пароль");
        btnChangePass.addActionListener(e -> onChangePassword());
        toolBar.add(btnChangePass);

        toolBar.addSeparator();
        JButton btnCert = new JButton("Выпуск USB-ключа");
        btnCert.addActionListener(e -> onIssueCertificate());
        toolBar.add(btnCert);

        btnAddUser = new JButton("Добавить");
        btnAddUser.addActionListener(e -> onAddUser());
        toolBar.add(btnAddUser);

        btnToggleBlock = new JButton("Блокировка");
        btnToggleBlock.addActionListener(e -> onToggleBlock());
        toolBar.add(btnToggleBlock);

        btnToggleRestrictions = new JButton("Ограничения");
        btnToggleRestrictions.addActionListener(e -> onToggleRestrictions());
        toolBar.add(btnToggleRestrictions);

        btnSetMinLength = new JButton("Мин. длина");
        btnSetMinLength.addActionListener(e -> onSetMinLength());
        toolBar.add(btnSetMinLength);

        toolBar.addSeparator();

        JButton btnAbout = new JButton("О программе");
        btnAbout.addActionListener(e -> onAbout());
        toolBar.add(btnAbout);

        JButton btnExit = new JButton("Выход");
        btnExit.addActionListener(e -> onExit());
        toolBar.add(btnExit);

        return toolBar;
    }


    private void applyAccessControl() {
        boolean isAdmin = currentUser.isAdmin();

        menuAdmin.setVisible(isAdmin);
        btnAddUser.setVisible(isAdmin);
        btnToggleBlock.setVisible(isAdmin);
        btnToggleRestrictions.setVisible(isAdmin);
        btnSetMinLength.setVisible(isAdmin);
    }

    private void updateStatusBar() {
        String roleStr = currentUser.isAdmin() ? "Администратор" : "Обычный пользователь";
        int totalUsers = userStore.getAllUsers().size();
        lblStatus.setText(String.format("Пользователь: %s | Роль: %s | Всего записей в БД: %d",
                currentUser.getUsername(), roleStr, totalUsers));
    }

    private User getSelectedUserOrWarn() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Выберите пользователя в таблице!",
                    "Предупреждение",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return tableModel.getUserAt(selectedRow);
    }

    private void refreshTable() {
        if (tableModel != null) {
            tableModel.setUsers(userStore.getAllUsers());
        }
        updateStatusBar();
    }


    private void onChangePassword() {
        ChangePasswordDialog dialog = new ChangePasswordDialog(this, currentUser, userStore);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            AuditLog.info("Пользователь '" + currentUser.getUsername() + "' сменил пароль");
            refreshTable();
        }
    }

    private void onAddUser() {
        String name = JOptionPane.showInputDialog(this,
                "Введите уникальное имя нового пользователя:",
                "Добавление пользователя",
                JOptionPane.PLAIN_MESSAGE);

        if (name == null || name.trim().isEmpty()) {
            return;
        }

        try {
            userStore.addUser(name.trim());
            AuditLog.info("ADMIN добавил ппользователя  '"+ name.trim() +"'");
            refreshTable();
            JOptionPane.showMessageDialog(this,
                    "Пользователь '" + name.trim() + "' успешно добавлен с пустым паролем!",
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка добавления: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onToggleBlock() {
        User target = getSelectedUserOrWarn();
        if (target == null) return;

        if (target.isAdmin()) {
            JOptionPane.showMessageDialog(this,
                    "Запрещено блокировать учетную запись администратора!",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean newState = !target.isBlocked();
        try {
            userStore.setBlocked(target.getUsername(), newState);
            AuditLog.info("ADMIN " + (newState ? "заблокировал":"разблокировал")+ " '" + target.getUsername() + "'");
            refreshTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onToggleRestrictions() {
        User target = getSelectedUserOrWarn();
        if (target == null) return;

        boolean newState = !target.isRestrictionsEnabled();
        try {
            userStore.setRestrictionsEnabled(target.getUsername(), newState);
            AuditLog.info("ADMIN" + (newState ? "включил":"выключил") + " ограничения пароля для'" + target.getUsername() + "'");
            refreshTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSetMinLength() {
        User target = getSelectedUserOrWarn();
        if (target == null) return;

        String input = JOptionPane.showInputDialog(this,
                "Введите минимальную длину пароля для пользователя '" + target.getUsername() + "':",
                String.valueOf(target.getMinLength()));

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        try {
            int len = Integer.parseInt(input.trim());
            if (len < 0 || len > 50) {
                throw new IllegalArgumentException("Длина должна быть в диапазоне от 0 до 50");
            }
            userStore.setMinLength(target.getUsername(), len);
            refreshTable();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Введите корректное число!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAbout() {
        AboutDialog dialog = new AboutDialog(this);
        dialog.setVisible(true);
    }

    private void onExit() {
        AuditLog.info("Выход пользователя '" + currentUser.getUsername() + "'");
        dispose();
        System.exit(0);
    }

    private void onIssueCertificate() {
        User target = getSelectedUserOrWarn();
        if (target == null) return;

        String currentDrive = UsbService.getConfiguredDrive();
        String drive = JOptionPane.showInputDialog(this,
                "Введите букву диска / путь к USB для записи сертификата:",
                currentDrive);

        if (drive == null || drive.trim().isEmpty()) {
            return;
        }

        try {
            UserCertificate cert = CertificateAuthority.issueCertificate(target.getUsername(), 30);


            UsbService.writeCertificateToDrive(drive.trim(), cert);

            target.setUseCertificate(true);
            userStore.save();
            refreshTable();

            AuditLog.info("ADMIN выпустил USB-сертификат для '" + target.getUsername() + "'");

            JOptionPane.showMessageDialog(this,
                    "Сертификат успешно выпущен и сохранён на " + drive.trim() + "!\n" + cert,
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка выпуска/записи сертификата: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onConfigureUsbDrive() {
        String currentDrive = UsbService.getConfiguredDrive();
        String newDrive = JOptionPane.showInputDialog(this,
                "Текущий рабочий диск для сертификатов:\n(например, E:\\ или C:\\temp)",
                currentDrive);

        if (newDrive != null && !newDrive.trim().isEmpty()) {
            try {
                UsbService.saveConfiguredDrive(newDrive.trim());
                JOptionPane.showMessageDialog(this,
                        "Настройки сохранены! Рабочий диск: " + newDrive.trim(),
                        "Настройки",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка сохранения настроек: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}