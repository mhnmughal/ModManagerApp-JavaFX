package org.mod.modmanager;


import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ModManagerPreloader extends Preloader {

    private ProgressBar progressBar;
    private Stage preloaderStage;

    @Override
    public void start(Stage stage) {
        this.preloaderStage = stage;

        // Create a simple layout with a ProgressBar
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);

        StackPane root = new StackPane(progressBar);
        Scene scene = new Scene(root, 400, 200);

        stage.setScene(scene);
        stage.setTitle("Loading Mod Manager...");
        stage.show();
    }

    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        progressBar.setProgress(pn.getProgress());
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
            preloaderStage.hide(); // Close preloader when main app is ready
        }
    }
}