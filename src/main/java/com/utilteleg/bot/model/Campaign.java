package com.utilteleg.bot.model;

import java.util.List;

public class Campaign {
    private String id;
    private String name;
    private List<Agency> agencies;

    // Constructors
    public Campaign() {
    }

    public Campaign(String id, String name, List<Agency> agencies) {
        this.id = id;
        this.name = name;
        this.agencies = agencies;
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

    public List<Agency> getAgencies() {
        return agencies;
    }

    public void setAgencies(List<Agency> agencies) {
        this.agencies = agencies;
    }
}