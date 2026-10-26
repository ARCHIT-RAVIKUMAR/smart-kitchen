package com.smartkitchen.entity;

public enum Unit {
    KG("kg"),
    G("g"),
    LITRE("litres"),
    ML("ml"),
    PACKET("packets"),
    PIECE("pieces");

    private final String label;

    Unit(String label) { this.label = label; }

    public String getLabel() { return label; }
}
