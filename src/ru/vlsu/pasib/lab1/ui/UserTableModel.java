package ru.vlsu.pasib.lab1.ui;

import ru.vlsu.pasib.lab1.model.User;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Модель данных таблицы пользователей для компонента JTable.
 */
public class UserTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {
            "№",
            "Имя пользователя",
            "Заблокирован",
            "Ограничения пароля",
            "Мин. длина пароля",
            "Сертификат USB"
    };

    private List<User> users;

    public UserTableModel(List<User> users) {
        this.users = users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        fireTableDataChanged(); // Уведомляет JTable о необходимости перерисовки
    }

    public User getUserAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < users.size()) {
            return users.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return users != null ? users.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        // За счёт точного указания типов Boolean JTable автоматически рисует чекбоксы
        return switch (columnIndex) {
            case 0 -> Integer.class;
            case 1 -> String.class;
            case 2, 3, 5 -> Boolean.class;
            case 4 -> Integer.class;
            default -> Object.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        User user = users.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> rowIndex + 1;
            case 1 -> user.getUsername();
            case 2 -> user.isBlocked();
            case 3 -> user.isRestrictionsEnabled();
            case 4 -> user.getMinLength();
            case 5 -> user.isUseCertificate();
            default -> null;
        };
    }
}