package ru.vlsu.pasib.lab1.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class AboutDialog extends JDialog {

    public AboutDialog(Frame parent) {
        super(parent, "О программе", true);

        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout(15, 15));
        rootPanel.setBorder(new EmptyBorder(15, 15, 15, 15));


        String infoHtml = "<html>"
                + "<h2 style='color: #0B3C5D; margin-bottom: 5px;'>Лабораторная работа №1</h2>"
                + "<b>Дисциплина:</b> Программно-аппаратные средства защиты информации<br>"
                + "<b>Тема:</b> Разработка ПО разграничения полномочий пользователей (СРД)<br>"
                + "<hr>"
                + "<b>Автор:</b> Блюменталь Иван Сергеевич<br>"
                + "<b>ВУЗ:</b> ВлГУ, кафедра ИЗИ<br>"
                + "<hr>"
                + "<b>Индивидуальное задание (Вариант П/Г-1):</b><br>"
                + "<ul>"
                + "  <li><b>Часть 1:</b> Политика сложности паролей:"
                + "    <ul>"
                + "      <li>Длина не менее индивидуально заданной администратором;</li>"
                + "      <li>Наличие строчных и прописных букв;</li>"
                + "      <li>Наличие цифр и знаков препинания;</li>"
                + "      <li>Отсутствие повторяющихся символов;</li>"
                + "      <li>Индикация выполнения каждого требования.</li>"
                + "    </ul>"
                + "  </li>"
                + "  <li><b>Часть 2:</b> Аутентификация по сертификату на USB-носителе.</li>"
                + "</ul>"
                + "</html>";

        JLabel lblInfo = new JLabel(infoHtml);
        rootPanel.add(lblInfo, BorderLayout.CENTER);

        JButton btnClose = new JButton("Закрыть");
        btnClose.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnClose);
        rootPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        getRootPane().setDefaultButton(btnClose);

        pack();
        setResizable(false);
        setLocationRelativeTo(getParent());
    }
}