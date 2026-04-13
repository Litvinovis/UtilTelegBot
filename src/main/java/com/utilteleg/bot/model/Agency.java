package com.utilteleg.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Agency(
        String id,
        String name,
        @JsonProperty("template-file") String templateFile,
        @JsonProperty("instruction-file") String instructionFile,
        @JsonProperty("delivery-options") List<String> deliveryOptions
) {
    public boolean hasTemplate() {
        return templateFile != null && !templateFile.isBlank();
    }

    public boolean hasInstruction() {
        return instructionFile != null && !instructionFile.isBlank();
    }
}
