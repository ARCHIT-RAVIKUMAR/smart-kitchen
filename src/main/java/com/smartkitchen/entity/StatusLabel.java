package com.smartkitchen.entity;

/**
 * ABSTRACTION + POLYMORPHISM: both StockStatus and ExpiryStatus can be shown
 * as a coloured badge. The HTML template only needs "a StatusLabel" and calls
 * getLabel()/getBadgeClass() - each enum answers in its own way.
 */
public interface StatusLabel {
    String getLabel();
    String getBadgeClass();
}
