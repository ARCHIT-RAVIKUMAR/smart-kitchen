package com.smartkitchen.exception;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(Long id) {
        super("Inventory item with ID " + id + " was not found.");
    }
}
