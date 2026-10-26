package com.smartkitchen.dto;

import com.smartkitchen.entity.ExpiryStatus;
import com.smartkitchen.entity.InventoryItem;
import com.smartkitchen.entity.StockStatus;

import java.math.BigDecimal;

/**
 * An item together with its calculated statuses. Statuses are never stored in
 * the database - they are calculated each time, so they are always correct.
 */
public class InventoryItemView {

    private final InventoryItem item;
    private final StockStatus stockStatus;
    private final ExpiryStatus expiryStatus;
    private final Long daysUntilExpiry;          // null when the item has no expiry date
    private final BigDecimal suggestedPurchase;  // how much to buy to restock

    public InventoryItemView(InventoryItem item, StockStatus stockStatus, ExpiryStatus expiryStatus,
                             Long daysUntilExpiry, BigDecimal suggestedPurchase) {
        this.item = item;
        this.stockStatus = stockStatus;
        this.expiryStatus = expiryStatus;
        this.daysUntilExpiry = daysUntilExpiry;
        this.suggestedPurchase = suggestedPurchase;
    }

    public InventoryItem getItem() { return item; }
    public StockStatus getStockStatus() { return stockStatus; }
    public ExpiryStatus getExpiryStatus() { return expiryStatus; }
    public Long getDaysUntilExpiry() { return daysUntilExpiry; }
    public BigDecimal getSuggestedPurchase() { return suggestedPurchase; }

    public String getQuantityWithUnit() {
        return plain(item.getQuantity()) + " " + item.getUnit().getLabel();
    }

    public String getMinimumWithUnit() {
        return plain(item.getMinimumStock()) + " " + item.getUnit().getLabel();
    }

    public String getSuggestedPurchaseWithUnit() {
        return plain(suggestedPurchase) + " " + item.getUnit().getLabel();
    }

    /** Short text such as "in 3 days", "today" or "2 days ago". Empty if no expiry date. */
    public String getExpiryNote() {
        if (daysUntilExpiry == null) {
            return "";
        }
        long d = daysUntilExpiry;
        if (d == 0) {
            return "today";
        }
        if (d > 0) {
            return "in " + d + (d == 1 ? " day" : " days");
        }
        long ago = -d;
        return ago + (ago == 1 ? " day ago" : " days ago");
    }

    private static String plain(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        if (stripped.scale() < 0) {
            stripped = stripped.setScale(0);
        }
        return stripped.toPlainString();
    }
}
