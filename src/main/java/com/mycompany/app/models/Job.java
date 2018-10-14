package com.mycompany.app.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Job {
    private Long id;

    private String name;

    public Job() {
        // Jackson deserialization
    }

    public Job(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @JsonProperty
    public Long getId() {
        return id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }
}
