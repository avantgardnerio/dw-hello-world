package com.mycompany.app.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Job {
    private Integer id;

    private String name;

    public Job() {
        // Jackson deserialization
    }

    public Job(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Objects.equals(id, job.id) &&
                Objects.equals(name, job.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
