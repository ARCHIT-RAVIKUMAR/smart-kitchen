package com.smartkitchen.service;

import com.smartkitchen.dto.CategoryCount;
import com.smartkitchen.dto.InventoryItemView;
import com.smartkitchen.entity.ActivityLog;
import com.smartkitchen.entity.Category;
import com.smartkitchen.entity.InventoryItem;
import com.smartkitchen.entity.Unit;
import com.smartkitchen.repository.ActivityLogRepository;
import com.smartkitchen.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceExtrasTest {

    @Mock
    private InventoryRepository repository;
    @Mock
    private ActivityLogRepository activityRepository;

    private InventoryStatusService statusService;
    private InventoryServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-25T10:00:00Z"), ZoneOffset.UTC);
        statusService = new InventoryStatusService(clock, 7);
        service = new InventoryServiceImpl(repository, activityRepository, statusService);
    }

    private static InventoryItem item(String name, Category category, String qty, String min) {
        return new InventoryItem(name, category, new BigDecimal(qty), Unit.KG, new BigDecimal(min), null, null, null);
    }

    private static void assertNumber(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    // ---------- suggested purchase rule ----------

    @Test
    void suggestedPurchaseRestocksToTwiceTheMinimum() {
        assertNumber("4", statusService.suggestedPurchase(new BigDecimal("0"), new BigDecimal("2")));
        assertNumber("3", statusService.suggestedPurchase(new BigDecimal("1"), new BigDecimal("2")));
        assertNumber("2", statusService.suggestedPurchase(new BigDecimal("2"), new BigDecimal("2")));
    }

    @Test
    void suggestedPurchaseIsOneWhenMinimumIsZeroAndNeverNegative() {
        assertNumber("1", statusService.suggestedPurchase(new BigDecimal("0"), new BigDecimal("0")));
        assertNumber("0", statusService.suggestedPurchase(new BigDecimal("9"), new BigDecimal("2")));
    }

    // ---------- adjust quantity ----------

    @Test
    void adjustQuantityAddsDelta() {
        InventoryItem existing = item("Rice", Category.GRAINS_CEREALS, "5", "2");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.adjustQuantity(1L, new BigDecimal("-1"));

        assertNumber("4", existing.getQuantity());
        verify(activityRepository).save(any(ActivityLog.class));
    }

    @Test
    void adjustQuantityBelowZeroIsRejected() {
        InventoryItem existing = item("Rice", Category.GRAINS_CEREALS, "1", "2");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> service.adjustQuantity(1L, new BigDecimal("-2")));
        assertNumber("1", existing.getQuantity());
        verify(repository, never()).save(any());
    }

    @Test
    void adjustQuantityWithoutNumberIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.adjustQuantity(1L, null));
    }

    // ---------- duplicate name ----------

    @Test
    void nameExistsTrimsAndDelegatesToRepository() {
        when(repository.existsByNameIgnoreCase("Rice")).thenReturn(true);
        assertTrue(service.nameExists("  Rice "));
    }

    @Test
    void nameExistsIsFalseForNull() {
        assertFalse(service.nameExists(null));
    }

    // ---------- shopping list ----------

    @Test
    void shoppingListHasOnlyLowAndOutItemsWithOutOfStockFirst() {
        when(repository.findAll()).thenReturn(List.of(
                item("Apples", Category.FRUITS_VEGETABLES, "5", "2"),   // in stock
                item("Salt", Category.SPICES_CONDIMENTS, "1", "2"),     // low
                item("Oil", Category.OTHER, "0", "1")));                // out

        List<InventoryItemView> list = service.getShoppingList();

        assertEquals(2, list.size());
        assertEquals("Oil", list.get(0).getItem().getName());
        assertEquals("Salt", list.get(1).getItem().getName());
        assertEquals("3 kg", list.get(1).getSuggestedPurchaseWithUnit());
    }

    // ---------- category breakdown ----------

    @Test
    void categoryBreakdownCountsOnlyUsedCategories() {
        when(repository.findAll()).thenReturn(List.of(
                item("Rice", Category.GRAINS_CEREALS, "5", "2"),
                item("Wheat", Category.GRAINS_CEREALS, "5", "2"),
                item("Milk", Category.DAIRY, "5", "2"),
                item("Tea", Category.BEVERAGES, "5", "2")));

        List<CategoryCount> result = service.getCategoryBreakdown();

        assertEquals(3, result.size());
        assertEquals("Grains & Cereals", result.get(0).getLabel());
        assertEquals(2, result.get(0).getCount());
        assertEquals(50, result.get(0).getPercent());
    }
}
