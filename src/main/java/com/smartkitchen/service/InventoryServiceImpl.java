package com.smartkitchen.service;

import com.smartkitchen.dto.Alert;
import com.smartkitchen.dto.CategoryCount;
import com.smartkitchen.dto.DashboardSummary;
import com.smartkitchen.dto.InventoryItemForm;
import com.smartkitchen.dto.InventoryItemView;
import com.smartkitchen.dto.ItemFilter;
import com.smartkitchen.entity.ActivityLog;
import com.smartkitchen.entity.ActivityType;
import com.smartkitchen.entity.Category;
import com.smartkitchen.entity.ExpiryStatus;
import com.smartkitchen.entity.InventoryItem;
import com.smartkitchen.entity.StockStatus;
import com.smartkitchen.exception.ItemNotFoundException;
import com.smartkitchen.repository.ActivityLogRepository;
import com.smartkitchen.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repository;
    private final ActivityLogRepository activityRepository;
    private final InventoryStatusService statusService;

    public InventoryServiceImpl(InventoryRepository repository,
                                ActivityLogRepository activityRepository,
                                InventoryStatusService statusService) {
        this.repository = repository;
        this.activityRepository = activityRepository;
        this.statusService = statusService;
    }

    // ------------------------------------------------------------ write operations

    @Override
    public InventoryItem create(InventoryItemForm form) {
        InventoryItem item = new InventoryItem(
                form.getName().trim(), form.getCategory(), form.getQuantity(), form.getUnit(),
                form.getMinimumStock(), form.getPurchaseDate(), form.getExpiryDate(),
                cleanNotes(form.getNotes()));
        InventoryItem saved = repository.save(item);
        log(ActivityType.ADDED, saved.getName(), "Quantity: " + plain(saved.getQuantity()) + " " + saved.getUnit().getLabel());
        return saved;
    }

    @Override
    public InventoryItem update(Long id, InventoryItemForm form) {
        InventoryItem item = findOrThrow(id);
        item.setName(form.getName().trim());
        item.setCategory(form.getCategory());
        item.setQuantity(form.getQuantity());
        item.setUnit(form.getUnit());
        item.setMinimumStock(form.getMinimumStock());
        item.setPurchaseDate(form.getPurchaseDate());
        item.setExpiryDate(form.getExpiryDate());
        item.setNotes(cleanNotes(form.getNotes()));
        InventoryItem saved = repository.save(item);
        log(ActivityType.UPDATED, saved.getName(), "Details edited");
        return saved;
    }

    @Override
    public void updateQuantity(Long id, BigDecimal newQuantity) {
        applyQuantity(findOrThrow(id), newQuantity);
    }

    @Override
    public void adjustQuantity(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("Please enter a number.");
        }
        InventoryItem item = findOrThrow(id);
        applyQuantity(item, item.getQuantity().add(delta));
    }

    private void applyQuantity(InventoryItem item, BigDecimal newQuantity) {
        BigDecimal old = item.getQuantity();
        item.setQuantity(newQuantity); // throws IllegalArgumentException if negative / null
        repository.save(item);
        log(ActivityType.QUANTITY_CHANGED, item.getName(),
                plain(old) + " -> " + plain(item.getQuantity()) + " " + item.getUnit().getLabel());
    }

    @Override
    public String delete(Long id) {
        InventoryItem item = findOrThrow(id);
        String name = item.getName();
        repository.delete(item);
        log(ActivityType.DELETED, name, "Removed from inventory");
        return name;
    }

    // ------------------------------------------------------------ read operations

    @Override
    @Transactional(readOnly = true)
    public InventoryItemForm getForm(Long id) {
        InventoryItem item = findOrThrow(id);
        InventoryItemForm form = new InventoryItemForm();
        form.setName(item.getName());
        form.setCategory(item.getCategory());
        form.setQuantity(item.getQuantity());
        form.setUnit(item.getUnit());
        form.setMinimumStock(item.getMinimumStock());
        form.setPurchaseDate(item.getPurchaseDate());
        form.setExpiryDate(item.getExpiryDate());
        form.setNotes(item.getNotes());
        return form;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemView getView(Long id) {
        return toView(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemView> search(ItemFilter filter) {
        String q = filter.getQ() == null ? "" : filter.getQ().trim().toLowerCase(Locale.ROOT);
        return allViews().stream()
                .filter(v -> q.isEmpty() || v.getItem().getName().toLowerCase(Locale.ROOT).contains(q))
                .filter(v -> filter.getCategory() == null || v.getItem().getCategory() == filter.getCategory())
                .filter(v -> filter.getStock() == null || v.getStockStatus() == filter.getStock())
                .filter(v -> filter.getExpiry() == null || v.getExpiryStatus() == filter.getExpiry())
                .sorted(comparatorFor(filter))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemView> itemsWithStockStatus(StockStatus status) {
        Comparator<InventoryItemView> byName =
                Comparator.comparing(v -> v.getItem().getName(), String.CASE_INSENSITIVE_ORDER);
        return allViews().stream()
                .filter(v -> v.getStockStatus() == status)
                .sorted(byName)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemView> itemsWithExpiryStatus(ExpiryStatus status) {
        Comparator<InventoryItemView> byDate = Comparator.comparing(v -> v.getItem().getExpiryDate());
        return allViews().stream()
                .filter(v -> v.getExpiryStatus() == status)
                .sorted(byDate)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean nameExists(String name) {
        return name != null && repository.existsByNameIgnoreCase(name.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemView> getShoppingList() {
        Comparator<InventoryItemView> outFirst =
                Comparator.comparingInt(v -> v.getStockStatus() == StockStatus.OUT_OF_STOCK ? 0 : 1);
        Comparator<InventoryItemView> byName =
                Comparator.comparing(v -> v.getItem().getName(), String.CASE_INSENSITIVE_ORDER);
        return allViews().stream()
                .filter(v -> v.getStockStatus() != StockStatus.IN_STOCK)
                .sorted(outFirst.thenComparing(byName))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryCount> getCategoryBreakdown() {
        List<InventoryItemView> views = allViews();
        List<CategoryCount> result = new ArrayList<>();
        for (Category category : Category.values()) {
            long n = views.stream().filter(v -> v.getItem().getCategory() == category).count();
            if (n > 0) {
                result.add(new CategoryCount(category.getLabel(), n, (int) Math.round(n * 100.0 / views.size())));
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return repository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummary getDashboardSummary() {
        List<InventoryItemView> views = allViews();
        return new DashboardSummary(
                views.size(),
                countStock(views, StockStatus.IN_STOCK),
                countStock(views, StockStatus.LOW_STOCK),
                countStock(views, StockStatus.OUT_OF_STOCK),
                views.stream().filter(v -> v.getExpiryStatus() == ExpiryStatus.EXPIRING_SOON).count(),
                views.stream().filter(v -> v.getExpiryStatus() == ExpiryStatus.EXPIRED).count());
    }

    /**
     * Builds one alert per item per condition (an item has at most one stock
     * alert and one expiry alert), so the same problem is never listed twice.
     * Most serious alerts come first.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Alert> getAlerts() {
        List<Alert> danger = new ArrayList<>();
        List<Alert> warning = new ArrayList<>();
        for (InventoryItemView v : allViews()) {
            InventoryItem item = v.getItem();
            switch (v.getStockStatus()) {
                case OUT_OF_STOCK -> danger.add(new Alert("danger", item.getName() + " is out of stock.", item.getId()));
                case LOW_STOCK -> warning.add(new Alert("warning", item.getName() + " is running low ("
                        + v.getQuantityWithUnit() + " left, minimum " + v.getMinimumWithUnit() + ").", item.getId()));
                default -> { }
            }
            switch (v.getExpiryStatus()) {
                case EXPIRED -> danger.add(new Alert("danger", item.getName() + " expired " + v.getExpiryNote() + ".", item.getId()));
                case EXPIRING_SOON -> warning.add(new Alert("warning", item.getName() + " expires " + v.getExpiryNote() + ".", item.getId()));
                default -> { }
            }
        }
        List<Alert> all = new ArrayList<>(danger);
        all.addAll(warning);
        return all;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLog> getRecentActivity() {
        return activityRepository.findTop8ByOrderByCreatedAtDescIdDesc();
    }

    @Override
    public int getExpiringSoonDays() {
        return statusService.getExpiringSoonDays();
    }

    // ------------------------------------------------------------ helpers

    private InventoryItem findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
    }

    private List<InventoryItemView> allViews() {
        return repository.findAll().stream().map(this::toView).toList();
    }

    private InventoryItemView toView(InventoryItem item) {
        return new InventoryItemView(item,
                statusService.stockStatus(item),
                statusService.expiryStatus(item.getExpiryDate()),
                statusService.daysUntilExpiry(item.getExpiryDate()),
                statusService.suggestedPurchase(item.getQuantity(), item.getMinimumStock()));
    }

    private long countStock(List<InventoryItemView> views, StockStatus status) {
        return views.stream().filter(v -> v.getStockStatus() == status).count();
    }

    private Comparator<InventoryItemView> comparatorFor(ItemFilter filter) {
        boolean desc = "desc".equalsIgnoreCase(filter.getDir());
        String sort = filter.getSort() == null ? "name" : filter.getSort();

        if ("expiry".equals(sort)) {
            // items without an expiry date always go to the bottom
            Comparator<LocalDate> dateOrder = desc
                    ? Comparator.nullsLast(Comparator.<LocalDate>reverseOrder())
                    : Comparator.nullsLast(Comparator.<LocalDate>naturalOrder());
            return Comparator.comparing(v -> v.getItem().getExpiryDate(), dateOrder);
        }

        Comparator<InventoryItemView> comparator;
        if ("quantity".equals(sort)) {
            comparator = Comparator.comparing(v -> v.getItem().getQuantity());
        } else {
            comparator = Comparator.comparing(v -> v.getItem().getName(), String.CASE_INSENSITIVE_ORDER);
        }
        return desc ? comparator.reversed() : comparator;
    }

    private void log(ActivityType type, String itemName, String details) {
        activityRepository.save(new ActivityLog(type, itemName, details));
    }

    private static String cleanNotes(String notes) {
        return (notes == null || notes.isBlank()) ? null : notes.trim();
    }

    private static String plain(BigDecimal value) {
        BigDecimal s = value.stripTrailingZeros();
        if (s.scale() < 0) {
            s = s.setScale(0);
        }
        return s.toPlainString();
    }
}
