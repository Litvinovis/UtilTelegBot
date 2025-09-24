package main.java.com.utilteleg.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Agency {
    private String id;
    private String name;
    private String description;
    
    @JsonProperty("template-file")
    private String templateFile;
    
    @JsonProperty("instruction-file")
    private String instructionFile;
    
    @JsonProperty("delivery-options")
    private List<String> deliveryOptions;

    // Constructors
    public Agency() {
    }

    public Agency(String id, String name, String description, String templateFile, String instructionFile, List<String> deliveryOptions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.templateFile = templateFile;
        this.instructionFile = instructionFile;
        this.deliveryOptions = deliveryOptions;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }

    public String getInstructionFile() {
        return instructionFile;
    }

    public void setInstructionFile(String instructionFile) {
        this.instructionFile = instructionFile;
    }

    public List<String> getDeliveryOptions() {
        return deliveryOptions;
    }

    public void setDeliveryOptions(List<String> deliveryOptions) {
        this.deliveryOptions = deliveryOptions;
    }
}