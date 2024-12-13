package org.mod.modmanager;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ModManagerController {

    @FXML
    private TextField gameFolderAddress;

    @FXML
    private TextField downloadFolderAddress;

    @FXML
    private TextField installFolderAddress;

    @FXML
    private Button gameFolder;

    @FXML
    private Button downloadFolder;

    @FXML
    private Button installFolder;

    @FXML
    private Button installMod;

    @FXML
    private Button revertMod;

    @FXML
    private ProgressBar progressBar;

    private File selectedGameFolder;
    private File selectedDownloadFile;
    private File selectedInstallFolder;

    @FXML
    public void initialize() {
        // Initialize ProgressBar
        progressBar.setProgress(0);
        progressBar.setVisible(false);

        // Game folder selection
        gameFolder.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Game Folder");
            File folder = chooser.showDialog(new Stage());
            if (folder != null) {
                selectedGameFolder = folder;
                gameFolderAddress.setText(folder.getAbsolutePath());
            }
        });

        // Download file selection
        downloadFolder.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Download File");
            File file = fileChooser.showOpenDialog(new Stage());
            if (file != null) {
                selectedDownloadFile = file;
                downloadFolderAddress.setText(file.getAbsolutePath());
            }
        });

        // Install folder selection
        installFolder.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Install Folder");
            File folder = chooser.showDialog(new Stage());
            if (folder != null) {
                selectedInstallFolder = folder;
                installFolderAddress.setText(folder.getAbsolutePath());
            }
        });

        // Install Mod
        installMod.setOnAction(event -> runTask(() -> {
            try {
                installMod();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Retro database Installation successful.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error during installation: " + e.getMessage());
            }
        }));

        // Revert Mod
        revertMod.setOnAction(event -> runTask(() -> {
            try {
                revertMod();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Modern database Installation successful.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error during reverting: " + e.getMessage());
            }
        }));
    }

    private void installMod() throws IOException {
        if (selectedGameFolder == null || selectedDownloadFile == null || selectedInstallFolder == null) {
            throw new IllegalStateException("Please select all required folders/files.");
        }

        // Rename folder
        File gameFolder = new File(selectedGameFolder, "Football Manager 2024");
        File modernFolder = new File(selectedGameFolder, "Football Manager Modern");
        if (!gameFolder.renameTo(modernFolder)) {
            throw new IOException("Failed to rename Football Manager 2024 folder.");
        }

        // Unzip the downloaded file
        File unzippedFolder = new File(selectedInstallFolder, "UnzippedContent");
        unzipFile(selectedDownloadFile, unzippedFolder);

        // Copy files
        Path source = unzippedFolder.toPath();
        Path target = new File(selectedGameFolder, "Football Manager").toPath();
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path targetPath = target.resolve(source.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Copy support staff file
        File supportStaffFile = new File(unzippedFolder, "support staff.edt");
        Files.copy(supportStaffFile.toPath(), new File(selectedInstallFolder, "support staff.edt").toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void revertMod() throws IOException {
        if (selectedGameFolder == null || selectedInstallFolder == null) {
            throw new IllegalStateException("Please select all required folders/files.");
        }

        // Rename folders
        File modernFolder = new File(selectedGameFolder, "Football Manager Modern");
        File retroFolder = new File(selectedGameFolder, "Football Manager Retro");
        File originalFolder = new File(selectedGameFolder, "Football Manager 2024");

        if (!modernFolder.renameTo(originalFolder)) {
            throw new IOException("Failed to rename Football Manager Modern folder back to Football Manager 2024.");
        }
        if (!retroFolder.renameTo(modernFolder)) {
            throw new IOException("Failed to rename Football Manager Retro folder.");
        }

        // Remove support staff file
        File supportStaffFile = new File(selectedInstallFolder, "support staff.edt");
        if (!supportStaffFile.delete()) {
            throw new IOException("Failed to delete support staff.edt.");
        }
    }

    private void unzipFile(File zipFile, File destDir) throws IOException {
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    newFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }

    private void runTask(Runnable task) {
        progressBar.setVisible(true);

        Task<Void> backgroundTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                task.run();
                return null;
            }
        };

        backgroundTask.setOnSucceeded(event -> {
            progressBar.setProgress(0);
            progressBar.setVisible(false);
        });

        backgroundTask.setOnFailed(event -> {
            progressBar.setProgress(0);
            progressBar.setVisible(false);
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        });

        new Thread(backgroundTask).start();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

