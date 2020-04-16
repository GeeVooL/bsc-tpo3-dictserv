package com.mdevv.tpo3.client;

import com.mdevv.tpo3.client.net.DictionaryService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Client extends Application {
    public static DictionaryService dictionaryService;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ui/ClientScene.fxml"));
        primaryStage.setTitle("Translator");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) throws IOException {
        String configurationPath = "";

        if (args.length > 0) {
            configurationPath = args[0];
        } else {
            System.err.println("This program requires one parameter - a path to configuration file.");
            System.exit(1);
        }

        Client.dictionaryService = new DictionaryService(configurationPath);
        Client.dictionaryService.startListening();
        Application.launch(args);
    }
}
