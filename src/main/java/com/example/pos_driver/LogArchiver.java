package com.example.pos_driver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class LogArchiver {

    @Value("${log.dir:logs}") // Fetch the current log directory dynamically
    private String logDir;

    private static final String ARCHIVE_DIR = "logs"; // Archive storage location

    @Scheduled(cron = "0 0 0 */7 * *") // Runs every 7 days at midnight//cron = "0 0 0 */7 * *"
    public void archiveLogs() {
        try {
            File logDirectory = new File(logDir);
            if (!logDirectory.exists() || !logDirectory.isDirectory()) {
                System.out.println("Log directory does not exist: " + logDir);
                return;
            }

            // Create archive folder if it doesn't exist
            File archiveDirectory = new File(ARCHIVE_DIR);
            if (!archiveDirectory.exists()) {
                archiveDirectory.mkdirs();
            }

            // Define the archive file name
            String archiveName = "logs-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip";
            File zipFile = new File(archiveDirectory, archiveName);

            // Create ZIP archive
            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                for (File logFile : logDirectory.listFiles()) {
                    if (logFile.isFile() && logFile.getName().endsWith(".log")) {
                        addToZip(logFile, zos);
                    }
                }
            }

            System.out.println("Logs archived successfully: " + zipFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addToZip(File file, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zos.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        }
    }
}
