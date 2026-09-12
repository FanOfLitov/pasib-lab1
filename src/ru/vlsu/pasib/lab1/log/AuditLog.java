package ru.vlsu.pasib.lab1.log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class AuditLog {

    private static final Path LOG_FILE = Paths.get("audit.log");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AuditLog() {
    }

    public static synchronized void info(String message) {
        write("INFO", message);
    }

    public static synchronized void warn(String message) {
        write("WARN", message);
    }

    public static synchronized void security(String message) {
        write("SECURITY", message);
    }

    private static void write(String level, String message) {
        String line = String.format("[%s] [%s] %s%n",
                LocalDateTime.now().format(FMT),
                level,
                message);
        try {
            Files.writeString(
                    LOG_FILE,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Не удалось записать audit.log: " + e.getMessage());
        }
    }


    public static synchronized void clear() throws IOException {
        Files.writeString(LOG_FILE, "", StandardCharsets.UTF_8);
    }

    public static Path getLogFile() {
        return LOG_FILE;
    }
}