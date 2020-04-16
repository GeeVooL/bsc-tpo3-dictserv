package com.mdevv.tpo3.client.ui;

import com.mdevv.tpo3.client.Client;
import com.mdevv.tpo3.client.net.DictionaryRequest;
import com.mdevv.tpo3.client.utils.TranslationStatus;
import com.mdevv.tpo3.common.Languages;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientScene implements Initializable {
    private static final String WAIT_TEXT = "Please wait...";
    private static final String ERROR_TEXT = "Error!";

    @FXML
    private TextField toTranslateTextField;
    @FXML
    private TextField translatedTextField;
    @FXML
    private ChoiceBox<String> languagesChoiceBox;
    @FXML
    private Button translateButton;

    ObservableList<String> languagesList = FXCollections.observableArrayList(Languages.getLanguagesList());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languagesChoiceBox.setItems(languagesList);
        languagesChoiceBox.setValue(Languages.DEFAULT);
    }

    @FXML
    public void translateButtonClicked(Event e) {
        Platform.runLater(() -> {
            disableControls(true);
            requestTranslation();
        });
    }

    private void disableControls(Boolean value) {
        translateButton.setDisable(value);
        languagesChoiceBox.setDisable(value);
        toTranslateTextField.setDisable(value);
        translatedTextField.setDisable(value);
    }

    private void requestTranslation() {
        DictionaryRequest request = Client.dictionaryService.createDictionaryRequest()
                .setLanguage(Languages.getCode(languagesChoiceBox.getValue()))
                .setWord(toTranslateTextField.getText())
                .setCallback((TranslationStatus status, String translated) ->
                        Platform.runLater(() -> {
                            switch (status) {
                                case ERROR:
                                    openErrorAlert("Dictionary server error.");
                                    return;
                                case NOT_FOUND:
                                    openErrorAlert("Word not found.");
                                    return;
                            }
                            translatedTextField.setText(translated);
                            disableControls(false);
                        })
                );
        TranslationStatus status = request.send();

        switch (status) {
            case ERROR:
                openErrorAlert("Server error occurred.");
                break;
            case NOT_SUPPORTED:
                openErrorAlert("Language not supported by the server.");
                break;
        }
    }

    private void openErrorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR, message, ButtonType.CLOSE);
        alert.showAndWait();
        disableControls(false);
    }
}