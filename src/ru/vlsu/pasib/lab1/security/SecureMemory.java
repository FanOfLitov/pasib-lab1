package ru.vlsu.pasib.lab1.security;

import java.util.Arrays;


public final class SecureMemory {

    private SecureMemory() {
    }


    public static void wipe(char[] data) {
        if (data == null) {
            return;
        }
        Arrays.fill(data, '\0');
        Arrays.fill(data, (char) 0xFFFF);
        Arrays.fill(data, '\0');
    }

    public static void wipe(byte[] data) {
        if (data == null) {
            return;
        }
        Arrays.fill(data, (byte) 0);
        Arrays.fill(data, (byte) 0xFF);
        Arrays.fill(data, (byte) 0);
    }


    public static void wipe(StringBuilder sb) {
        if (sb == null) {
            return;
        }
        for (int i = 0; i < sb.length(); i++) {
            sb.setCharAt(i, '\0');
        }
        sb.setLength(0);
    }
}