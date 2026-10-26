package com.smartkitchen.service;

import com.smartkitchen.dto.Alert;
import com.smartkitchen.dto.CategoryCount;
import com.smartkitchen.dto.DashboardSummary;
import com.smartkitchen.dto.InventoryItemForm;
import com.smartkitchen.dto.InventoryItemView;
import com.smartkitchen.dto.ItemFilter;
import com.smartkitchen.entity.ActivityLog;
import com.smartkitchen.entity.ExpiryStatus;
import com.smartkitchen.entity.InventoryItem;
import com.smartkitchen.entity.StockStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * ABSTRACTION: controllers depend on this interface, not on the implementation.
 * The implementation (InventoryServiceImpl) can change without touching controllers.
 */
public interface InventoryService {

    InventoryItem create(InventoryItemForm form);

    InventoryItem update(Long id, InventoryItemForm form);

    void updateQuantity(Long id, BigDecimal newQuantity);

    /** Adds (or, if negative, removes) an amount, e.g. -1 after using one packet. */
    void adjustQuantity(Long id, BigDecimal delta);

    /** True if an item with this name (ignoring upper/lower case) already exists. */
    boolean nameExists(String name);

    /** Items that are low or out of stock, most urgent first. */
    List<InventoryItemView> getShoppingList();

    List<CategoryCount> getCategoryBreakdown();

    /** Deletes the item and returns its name (for the success message). */
    String delete(Long id);

    InventoryItemForm getForm(Long id);

    InventoryItemView getView(Long id);

    List<InventoryItemView> search(ItemFilter filter);

    List<InventoryItemView> itemsWithStockStatus(StockStatus status);

    List<InventoryItemView> itemsWithExpiryStatus(ExpiryStatus status);

    long countAll();

    DashboardSummary getDashboardSummary();

    List<Alert> getAlerts();

    List<ActivityLog> getRecentActivity();

    int getExpiringSoonDays();
}
