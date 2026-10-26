package com.smartkitchen.entity;

public enum StockStatus implements StatusLabel {
    IN_STOCK("In Stock", "text-bg-success"),
    LOW_STOCK("Low Stock", "text-bg-warning"),
    OUT_OF_STOCK("Out of Stock", "text-bg-danger");

    private final String label;
    private final String badgeClass;

    StockStatus(String label, String badgeClass) {
        this.label = label;
        this.badgeClass = badgeClass;
    }

    @Override
    public String getLabel() { return label; }

    @Override
    public String getBadgeClass() { return badgeClass; }
}
