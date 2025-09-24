package main.java.com.utilteleg.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.utilteleg.bot.model.Campaign;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TestConfigLoading {
    public static void main(String[] args) {
        try {
            // Test direct YAML parsing
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Map<String, Object> yamlData = mapper.readValue(
                new File("src/main/resources/application.yml"), 
                Map.class
            );
            
            System.out.println("YAML file loaded successfully!");
            System.out.println("Root keys: " + yamlData.keySet());
            
            List<Map<String, Object>> campaigns = (List<Map<String, Object>>) yamlData.get("campaigns");
            System.out.println("Number of campaigns: " + campaigns.size());
            
            for (int i = 0; i < campaigns.size(); i++) {
                Map<String, Object> campaign = campaigns.get(i);
                System.out.println("Campaign " + (i+1) + ": " + campaign.get("name"));
                List<Map<String, Object>> agencies = (List<Map<String, Object>>) campaign.get("agencies");
                System.out.println("  Number of agencies: " + agencies.size());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}