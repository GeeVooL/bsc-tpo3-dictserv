package com.mdevv.tpo3.dict.components;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Translator {
    private final JSONObject dict;
    private final String language;

    public Translator(Path dictPath, String language) throws IOException {
        String fileContent = new String(Files.readAllBytes(dictPath));
        this.dict = new JSONObject(fileContent);
        this.language = language;
    }

    public String translate(String word) {
        String translated = null;
        try {
            translated = dict.getString(word);
        } catch (JSONException e) {
            System.err.println("Word " + word + " not found.");
        }
        return translated;
    }

    public String getLanguage() {
        return language;
    }
}
