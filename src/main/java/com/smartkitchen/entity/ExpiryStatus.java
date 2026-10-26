package com.smartkitchen.entity;

public enum ExpiryStatus implements StatusLabel {
    NO_EXPIRY("No Expiry", "text-bg-secondary"),
    FRESH("Fresh", "text-bg-success"),
    EXPIRING_SOON("Expiring Soon", "text-bg-warning"),
    EXPIRED("Expired", "text-bg-danger");

    private final String label;
    private final String badgeClass;

    ExpiryStatus(String label, String badgeClass) {
        this.label = label;
        this.badgeClass = badgeClass;
    }

    @Override
    public String getLabel() { return label; }

    @Override
    public String getBadgeClass() { return badgeClass; }
}
