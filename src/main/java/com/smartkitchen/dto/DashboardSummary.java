package com.smartkitchen.dto;

/** The numbers shown on the dashboard cards. */
public class DashboardSummary {

    private final long total;
    private final long inStock;
    private final long lowStock;
    private final long outOfStock;
    private final long expiringSoon;
    private final long expired;

    public DashboardSummary(long total, long inStock, long lowStock, long outOfStock,
                            long expiringSoon, long expired) {
        this.total = total;
        this.inStock = inStock;
        this.lowStock = lowStock;
        this.outOfStock = outOfStock;
        this.expiringSoon = expiringSoon;
        this.expired = expired;
    }

    public long getTotal() { return total; }
    public long getInStock() { return inStock; }
    public long getLowStock() { return lowStock; }
    public long getOutOfStock() { return outOfStock; }
    public long getExpiringSoon() { return expiringSoon; }
    public long getExpired() { return expired; }
}
