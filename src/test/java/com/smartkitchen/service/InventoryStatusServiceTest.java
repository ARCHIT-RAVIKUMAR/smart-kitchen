package com.smartkitchen.service;

import com.smartkitchen.entity.ExpiryStatus;
import com.smartkitchen.entity.StockStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** "Today" is frozen at 25 Oct 2026 so boundary dates are tested exactly. */
class InventoryStatusServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 25);
    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-10-25T10:00:00Z"), ZoneOffset.UTC);
    private final InventoryStatusService service = new InventoryStatusService(fixedClock, 7);

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    // ---------- stock status ----------

    @Test
    void zeroQuantityIsOutOfStockNotLow() {
        assertEquals(StockStatus.OUT_OF_STOCK, service.stockStatus(bd("0"), bd("2")));
    }

    @Test
    void zeroQuantityWithZeroThresholdIsStillOutOfStock() {
        assertEquals(StockStatus.OUT_OF_STOCK, service.stockStatus(bd("0"), bd("0")));
    }

    @Test
    void quantityEqualToThresholdIsLowStock() {
        assertEquals(StockStatus.LOW_STOCK, service.stockStatus(bd("2"), bd("2")));
    }

    @Test
    void quantityBelowThresholdIsLowStock() {
        assertEquals(StockStatus.LOW_STOCK, service.stockStatus(bd("0.5"), bd("2")));
    }

    @Test
    void quantityJustAboveThresholdIsInStock() {
        assertEquals(StockStatus.IN_STOCK, service.stockStatus(bd("2.01"), bd("2")));
    }

    @Test
    void positiveQuantityWithZeroThresholdIsInStock() {
        assertEquals(StockStatus.IN_STOCK, service.stockStatus(bd("1"), bd("0")));
    }

    // ---------- expiry status ----------

    @Test
    void noExpiryDateIsNeverExpired() {
        assertEquals(ExpiryStatus.NO_EXPIRY, service.expiryStatus(null));
        assertNull(service.daysUntilExpiry(null));
    }

    @Test
    void yesterdayIsExpired() {
        assertEquals(ExpiryStatus.EXPIRED, service.expiryStatus(TODAY.minusDays(1)));
    }

    @Test
    void todayIsExpiringSoonNotExpired() {
        assertEquals(ExpiryStatus.EXPIRING_SOON, service.expiryStatus(TODAY));
    }

    @Test
    void exactlySevenDaysAwayIsExpiringSoon() {
        assertEquals(ExpiryStatus.EXPIRING_SOON, service.expiryStatus(TODAY.plusDays(7)));
    }

    @Test
    void eightDaysAwayIsFresh() {
        assertEquals(ExpiryStatus.FRESH, service.expiryStatus(TODAY.plusDays(8)));
    }

    @Test
    void expiredItemIsNeverExpiringSoon() {
        assertEquals(ExpiryStatus.EXPIRED, service.expiryStatus(TODAY.minusDays(30)));
    }

    @Test
    void daysUntilExpiryIsNegativeForPastDates() {
        assertEquals(-3L, service.daysUntilExpiry(TODAY.minusDays(3)));
        assertEquals(5L, service.daysUntilExpiry(TODAY.plusDays(5)));
    }

    @Test
    void expiringSoonWindowIsConfigurable() {
        InventoryStatusService threeDays = new InventoryStatusService(fixedClock, 3);
        assertEquals(ExpiryStatus.EXPIRING_SOON, threeDays.expiryStatus(TODAY.plusDays(3)));
        assertEquals(ExpiryStatus.FRESH, threeDays.expiryStatus(TODAY.plusDays(4)));
    }
}
