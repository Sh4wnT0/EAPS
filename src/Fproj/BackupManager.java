package Fproj;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupManager {

    private static final String DB_FILE = "employees.db";
    private static final String[] FOLDERS_TO_BACKUP = {"employee_photos", "resume_uploads"};
    private static final String BACKUP_DIR = "backups";
    
    // Config: How many recent backups to keep?
    private static final int MAX_BACKUPS = 5; 

    public static void createBackup() throws IOException {
        Path backupPath = Paths.get(BACKUP_DIR);
        if (!Files.exists(backupPath)) Files.createDirectories(backupPath);

        // 1. Create the NEW Backup
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String zipFileName = "backup_" + timestamp + ".zip";
        Path zipFilePath = backupPath.resolve(zipFileName);

        try (FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            addToZip(new File(DB_FILE), zos);

            for (String folderName : FOLDERS_TO_BACKUP) {
                File folder = new File(folderName);
                if (folder.exists()) {
                    addFolderToZip(folder, folder.getName(), zos);
                }
            }
        }
        
        // 2. DELETE Old Backups (Cleanup)
        cleanOldBackups(backupPath.toFile());
    }

    // --- NEW: Cleanup Logic ---
    private static void cleanOldBackups(File backupDir) {
        File[] files = backupDir.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".zip"));
        
        if (files != null && files.length > MAX_BACKUPS) {
            // Sort by Last Modified (Oldest first)
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            
            // Delete extra files
            int filesToDelete = files.length - MAX_BACKUPS;
            for (int i = 0; i < filesToDelete; i++) {
                if (files[i].delete()) {
                    System.out.println("Deleted old backup: " + files[i].getName());
                }
            }
        }
    }

    // --- Existing Helper Methods (Same as before) ---
    private static void addToZip(File file, ZipOutputStream zos) throws IOException {
        if (!file.exists()) return;
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zos.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) zos.write(bytes, 0, length);
            zos.closeEntry();
        }
    }

    private static void addFolderToZip(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFolderToZip(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }
            try (FileInputStream fis = new FileInputStream(file)) {
                ZipEntry zipEntry = new ZipEntry(parentFolder + "/" + file.getName());
                zos.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) zos.write(bytes, 0, length);
                zos.closeEntry();
            }
        }
    }
}
