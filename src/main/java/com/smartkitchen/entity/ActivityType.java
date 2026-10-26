package com.smartkitchen.entity;

public enum ActivityType {
    ADDED("Added"),
    UPDATED("Updated"),
    QUANTITY_CHANGED("Quantity changed"),
    DELETED("Deleted");

    private final String label;

    ActivityType(String label) { this.label = label; }

    public String getLabel() { return label; }
}
