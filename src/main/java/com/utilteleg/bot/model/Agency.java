package com.utilteleg.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Agency {
    private String id;
    private String name;
    
    @JsonProperty("template-file")
    private String templateFile;
    
    @JsonProperty("instruction-file")
    private String instructionFile;
    
    @JsonProperty("delivery-options")
    private List<String> deliveryOptions;

    // Constructors
    public Agency() {
    }

    public Agency(String id, String name, String templateFile, String instructionFile, List<String> deliveryOptions) {
        this.id = id;
        this.name = name;
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
    
    /**
     * Checks if this agency has a template file configured
     * @return true if template file is configured and not empty
     */
    public boolean hasTemplate() {
        return templateFile != null && !templateFile.trim().isEmpty();
    }
    
    /**
     * Checks if this agency has an instruction file configured
     * @return true if instruction file is configured and not empty
     */
    public boolean hasInstruction() {
        return instructionFile != null && !instructionFile.trim().isEmpty();
    }
}