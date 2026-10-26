package com.smartkitchen.config;

import com.smartkitchen.entity.Category;
import com.smartkitchen.entity.InventoryItem;
import com.smartkitchen.entity.Unit;
import com.smartkitchen.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Adds demo items ONLY when the table is empty, so restarting the app never
 * creates duplicates. Turn off with SEED_DEMO_DATA=false.
 * Dates are relative to today so the demo always shows every status.
 */
@Component
@ConditionalOnProperty(name = "kitchen.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataLoader implements CommandLineRunner {

    private final InventoryRepository repository;
    private final Clock clock;

    public DemoDataLoader(InventoryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now(clock);
        repository.saveAll(List.of(
                new InventoryItem("Basmati Rice", Category.GRAINS_CEREALS, bd("5"), Unit.KG, bd("2"),
                        today.minusDays(20), today.plusDays(300), "Long grain, 5 kg bag"),
                new InventoryItem("Wheat Flour", Category.GRAINS_CEREALS, bd("1"), Unit.KG, bd("2"),
                        today.minusDays(10), today.plusDays(120), null),
                new InventoryItem("Milk", Category.DAIRY, bd("2"), Unit.LITRE, bd("1"),
                        today.minusDays(2), today.plusDays(2), "Keep refrigerated"),
                new InventoryItem("Yogurt", Category.DAIRY, bd("0"), Unit.PACKET, bd("1"),
                        today.minusDays(6), today.plusDays(4), null),
                new InventoryItem("Spinach", Category.FRUITS_VEGETABLES, bd("0.5"), Unit.KG, bd("0.25"),
                        today.minusDays(6), today.minusDays(1), "Past its date - discard"),
                new InventoryItem("Tomatoes", Category.FRUITS_VEGETABLES, bd("3"), Unit.KG, bd("1"),
                        today.minusDays(2), today.plusDays(5), null),
                new InventoryItem("Chicken", Category.MEAT_SEAFOOD, bd("1"), Unit.KG, bd("1"),
                        today.minusDays(1), today.plusDays(1), "Freezer shelf"),
                new InventoryItem("Turmeric Powder", Category.SPICES_CONDIMENTS, bd("200"), Unit.G, bd("50"),
                        today.minusDays(60), today.plusDays(200), null),
                new InventoryItem("Salt", Category.SPICES_CONDIMENTS, bd("2"), Unit.KG, bd("1"),
                        null, null, "Salt does not expire"),
                new InventoryItem("Biscuits", Category.SNACKS, bd("4"), Unit.PACKET, bd("2"),
                        today.minusDays(5), today.plusDays(40), null),
                new InventoryItem("Orange Juice", Category.BEVERAGES, bd("3"), Unit.LITRE, bd("2"),
                        today.minusDays(3), today.plusDays(10), null)
        ));
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
