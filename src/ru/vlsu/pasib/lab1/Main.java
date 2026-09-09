package ru.vlsu.pasib.lab1;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args){
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