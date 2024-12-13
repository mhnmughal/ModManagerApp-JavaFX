package org.mod.modmanager;


import javafx.application.Application;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void init() throws Exception {
        // Simulate long-running initialization
        for (int i = 0; i < 10; i++) {
            Thread.sleep(300); // Simulated delay
            notifyPreloader(new Preloader.ProgressNotification((i + 1) / 10.0));
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ModManager.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Mod Manager");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
        System.out.println("JavaFX Version: " + System.getProperty("javafx.version"));
    }
}
