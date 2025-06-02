package com.example.project_generator.model;

public class FieldDefinition {
    private String name;
    private String type; 
    private boolean primaryKey;
    private boolean notNull;

   

    public FieldDefinition(String name, String type, boolean primaryKey, boolean notNull) {
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
        this.notNull = notNull;
    }

   
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }
}

