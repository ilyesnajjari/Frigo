package fr.tse.fise2.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ConfigReader {
    private String apiKey;

    public ConfigReader() {
        // Instantiate the object, but do not load the configuration in the constructor
    }

    public void loadConfig() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Load configuration from the config.json file
            ConfigReader config = objectMapper.readValue(new File("src/main/resources/config.json"), ConfigReader.class);

            // Set the API key
            this.apiKey = config.apiKey;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getApiKey() {
        return apiKey;
    }
}
