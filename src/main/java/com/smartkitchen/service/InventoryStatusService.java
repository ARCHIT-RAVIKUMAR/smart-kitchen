package com.smartkitchen.service;

import com.smartkitchen.entity.ExpiryStatus;
import com.smartkitchen.entity.InventoryItem;
import com.smartkitchen.entity.StockStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * The ONLY place where stock and expiry rules are written.
 *
 * Stock rules:
 *   quantity = 0                       -> OUT_OF_STOCK
 *   0 < quantity <= minimum threshold  -> LOW_STOCK
 *   quantity > minimum threshold       -> IN_STOCK
 *
 * Expiry rules (N = "expiring soon" days, default 7):
 *   no expiry date                     -> NO_EXPIRY
 *   expiry date before today           -> EXPIRED
 *   today <= expiry date <= today + N  -> EXPIRING_SOON
 *   later than today + N               -> FRESH
 */
@Service
public class InventoryStatusService {

    private final Clock clock;
    private final int expiringSoonDays;

    public InventoryStatusService(Clock clock,
                                  @Value("${kitchen.expiry-soon-days:7}") int expiringSoonDays) {
        this.clock = clock;
        this.expiringSoonDays = expiringSoonDays;
    }

    public StockStatus stockStatus(InventoryItem item) {
        return stockStatus(item.getQuantity(), item.getMinimumStock());
    }

    public StockStatus stockStatus(BigDecimal quantity, BigDecimal minimumStock) {
        if (quantity.signum() <= 0) {
            return StockStatus.OUT_OF_STOCK;
        }
        if (quantity.compareTo(minimumStock) <= 0) {
            return StockStatus.LOW_STOCK;
        }
        return StockStatus.IN_STOCK;
    }

    public ExpiryStatus expiryStatus(LocalDate expiryDate) {
        if (expiryDate == null) {
            return ExpiryStatus.NO_EXPIRY;
        }
        long days = daysUntilExpiry(expiryDate);
        if (days < 0) {
            return ExpiryStatus.EXPIRED;
        }
        if (days <= expiringSoonDays) {
            return ExpiryStatus.EXPIRING_SOON;
        }
        return ExpiryStatus.FRESH;
    }

    /** Days from today to the expiry date (negative = already expired). */
    public Long daysUntilExpiry(LocalDate expiryDate) {
        if (expiryDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(clock), expiryDate);
    }

    /**
     * Shopping-list rule: restock up to TWICE the minimum stock
     * (or 1 unit when the minimum is 0). Never negative.
     */
    public BigDecimal suggestedPurchase(BigDecimal quantity, BigDecimal minimumStock) {
        BigDecimal target = minimumStock.signum() == 0
                ? BigDecimal.ONE
                : minimumStock.multiply(BigDecimal.valueOf(2));
        BigDecimal toBuy = target.subtract(quantity);
        return toBuy.signum() < 0 ? BigDecimal.ZERO : toBuy;
    }

    public int getExpiringSoonDays() {
        return expiringSoonDays;
    }
}
