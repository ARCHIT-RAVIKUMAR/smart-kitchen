package com.smartkitchen.service;

import com.smartkitchen.dto.InventoryItemForm;
import com.smartkitchen.dto.InventoryItemView;
import com.smartkitchen.dto.ItemFilter;
import com.smartkitchen.entity.ActivityLog;
import com.smartkitchen.entity.Category;
import com.smartkitchen.entity.InventoryItem;
import com.smartkitchen.entity.StockStatus;
import com.smartkitchen.entity.Unit;
import com.smartkitchen.exception.ItemNotFoundException;
import com.smartkitchen.repository.ActivityLogRepository;
import com.smartkitchen.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository repository;
    @Mock
    private ActivityLogRepository activityRepository;

    private InventoryServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-25T10:00:00Z"), ZoneOffset.UTC);
        service = new InventoryServiceImpl(repository, activityRepository, new InventoryStatusService(clock, 7));
    }

    private static InventoryItemForm form(String name, String qty) {
        InventoryItemForm f = new InventoryItemForm();
        f.setName(name);
        f.setCategory(Category.GRAINS_CEREALS);
        f.setQuantity(new BigDecimal(qty));
        f.setUnit(Unit.KG);
        f.setMinimumStock(new BigDecimal("2"));
        f.setNotes("   ");
        return f;
    }

    private static InventoryItem item(String name, String qty) {
        return new InventoryItem(name, Category.GRAINS_CEREALS, new BigDecimal(qty), Unit.KG,
                new BigDecimal("2"), null, null, null);
    }

    @Test
    void createTrimsNameCleansNotesAndLogsActivity() {
        when(repository.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryItem saved = service.create(form("  Rice  ", "5"));

        assertEquals("Rice", saved.getName());
        assertEquals(null, saved.getNotes());
        verify(activityRepository).save(any(ActivityLog.class));
    }

    @Test
    void updateChangesAllFieldsAndLogsActivity() {
        InventoryItem existing = item("Rice", "5");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        InventoryItem updated = service.update(1L, form("Brown Rice", "9"));

        assertEquals("Brown Rice", updated.getName());
        assertEquals(new BigDecimal("9"), updated.getQuantity());
        verify(activityRepository).save(any(ActivityLog.class));
    }

    @Test
    void updateMissingItemThrowsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class, () -> service.update(99L, form("X", "1")));
        verify(repository, never()).save(any());
    }

    @Test
    void updateQuantityRejectsNegativeValues() {
        InventoryItem existing = item("Rice", "5");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> service.updateQuantity(1L, new BigDecimal("-1")));
        assertEquals(new BigDecimal("5"), existing.getQuantity());
        verify(repository, never()).save(any());
    }

    @Test
    void updateQuantitySavesNewValueAndLogs() {
        InventoryItem existing = item("Rice", "5");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.updateQuantity(1L, new BigDecimal("0"));

        assertEquals(0, existing.getQuantity().signum());
        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityRepository).save(captor.capture());
        assertEquals("Rice", captor.getValue().getItemName());
    }

    @Test
    void deleteReturnsNameAndRemovesItem() {
        InventoryItem existing = item("Rice", "5");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertEquals("Rice", service.delete(1L));
        verify(repository).delete(existing);
    }

    @Test
    void getViewForMissingIdThrowsNotFound() {
        when(repository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class, () -> service.getView(5L));
    }

    @Test
    void searchFiltersByNameAndSortsByQuantityDescending() {
        when(repository.findAll()).thenReturn(List.of(
                item("Rice", "5"), item("Rice Flour", "9"), item("Salt", "1")));

        ItemFilter filter = new ItemFilter();
        filter.setQ("rice");
        filter.setSort("quantity");
        filter.setDir("desc");

        List<InventoryItemView> result = service.search(filter);

        assertEquals(2, result.size());
        assertEquals("Rice Flour", result.get(0).getItem().getName());
        assertEquals("Rice", result.get(1).getItem().getName());
    }

    @Test
    void searchFiltersByStockStatus() {
        when(repository.findAll()).thenReturn(List.of(item("Rice", "5"), item("Salt", "1"), item("Oil", "0")));

        ItemFilter filter = new ItemFilter();
        filter.setStock(StockStatus.LOW_STOCK);

        List<InventoryItemView> result = service.search(filter);

        assertEquals(1, result.size());
        assertEquals("Salt", result.get(0).getItem().getName());
    }

    @Test
    void dashboardCountsMatchStatuses() {
        when(repository.findAll()).thenReturn(List.of(item("Rice", "5"), item("Salt", "1"), item("Oil", "0")));

        var summary = service.getDashboardSummary();

        assertEquals(3, summary.getTotal());
        assertEquals(1, summary.getInStock());
        assertEquals(1, summary.getLowStock());
        assertEquals(1, summary.getOutOfStock());
    }

    @Test
    void alertsContainOneEntryPerConditionPerItem() {
        when(repository.findAll()).thenReturn(List.of(item("Rice", "5"), item("Salt", "1"), item("Oil", "0")));

        var alerts = service.getAlerts();

        assertEquals(2, alerts.size()); // Oil out of stock + Salt low stock
        assertEquals("danger", alerts.get(0).getSeverity()); // most serious first
    }
}
