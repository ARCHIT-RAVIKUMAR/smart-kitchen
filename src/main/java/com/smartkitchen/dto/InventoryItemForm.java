package com.smartkitchen.dto;

import com.smartkitchen.entity.Category;
import com.smartkitchen.entity.Unit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object): carries the data typed into the Add/Edit form.
 * The validation rules live here, so bad input is rejected before it reaches
 * the database entity.
 */
public class InventoryItemForm {

    @NotBlank(message = "Item name is required.")
    @Size(max = 100, message = "Item name can be at most 100 characters.")
    private String name;

    @NotNull(message = "Please choose a category.")
    private Category category;

    @NotNull(message = "Quantity is required.")
    @DecimalMin(value = "0.0", message = "Quantity cannot be negative.")
    @Digits(integer = 10, fraction = 2, message = "Use at most 10 digits and 2 decimal places.")
    private BigDecimal quantity;

    @NotNull(message = "Please choose a unit.")
    private Unit unit;

    @NotNull(message = "Minimum stock is required.")
    @DecimalMin(value = "0.0", message = "Minimum stock cannot be negative.")
    @Digits(integer = 10, fraction = 2, message = "Use at most 10 digits and 2 decimal places.")
    private BigDecimal minimumStock;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchaseDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryDate;

    @Size(max = 500, message = "Notes can be at most 500 characters.")
    private String notes;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

    public BigDecimal getMinimumStock() { return minimumStock; }
    public void setMinimumStock(BigDecimal minimumStock) { this.minimumStock = minimumStock; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
