package com.smartkitchen.entity;

/** The fixed list of kitchen categories shown in the forms and filters. */
public enum Category {
    GRAINS_CEREALS("Grains & Cereals"),
    FRUITS_VEGETABLES("Fruits & Vegetables"),
    DAIRY("Dairy Products"),
    MEAT_SEAFOOD("Meat & Seafood"),
    SPICES_CONDIMENTS("Spices & Condiments"),
    SNACKS("Snacks"),
    BEVERAGES("Beverages"),
    OTHER("Other");

    private final String label;

    Category(String label) { this.label = label; }

    public String getLabel() { return label; }
}
