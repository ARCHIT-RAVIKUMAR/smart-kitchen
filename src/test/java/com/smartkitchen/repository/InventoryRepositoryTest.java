package com.smartkitchen.repository;

import com.smartkitchen.entity.ActivityLog;
import com.smartkitchen.entity.ActivityType;
import com.smartkitchen.entity.Category;
import com.smartkitchen.entity.InventoryItem;
import com.smartkitchen.entity.Unit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository repository;
    @Autowired
    private ActivityLogRepository activityRepository;

    @Test
    void savesItemWithoutExpiryDateAndFindsItAgain() {
        InventoryItem saved = repository.saveAndFlush(new InventoryItem("Salt", Category.SPICES_CONDIMENTS,
                new BigDecimal("2"), Unit.KG, new BigDecimal("1"), null, null, null));

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        InventoryItem found = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Salt", found.getName());
        assertNull(found.getExpiryDate());
    }

    @Test
    void savesAndDeletesItem() {
        InventoryItem saved = repository.saveAndFlush(new InventoryItem("Milk", Category.DAIRY,
                new BigDecimal("1.5"), Unit.LITRE, new BigDecimal("1"),
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 30), "Fridge"));

        repository.deleteById(saved.getId());
        repository.flush();

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }

    @Test
    void recentActivityReturnsAtMostEightNewestFirst() {
        for (int i = 1; i <= 10; i++) {
            activityRepository.save(new ActivityLog(ActivityType.ADDED, "Item " + i, "test"));
        }
        activityRepository.flush();

        List<ActivityLog> recent = activityRepository.findTop8ByOrderByCreatedAtDescIdDesc();

        assertEquals(8, recent.size());
        assertEquals("Item 10", recent.get(0).getItemName());
    }
}
