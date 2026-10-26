package com.smartkitchen.dto;

import com.smartkitchen.entity.Category;
import com.smartkitchen.entity.ExpiryStatus;
import com.smartkitchen.entity.StockStatus;

/** Search / filter / sort choices from the inventory list page. */
public class ItemFilter {

    private String q;
    private Category category;
    private StockStatus stock;
    private ExpiryStatus expiry;
    private String sort = "name";   // name | quantity | expiry
    private String dir = "asc";     // asc | desc

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public StockStatus getStock() { return stock; }
    public void setStock(StockStatus stock) { this.stock = stock; }

    public ExpiryStatus getExpiry() { return expiry; }
    public void setExpiry(ExpiryStatus expiry) { this.expiry = expiry; }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }

    public String getDir() { return dir; }
    public void setDir(String dir) { this.dir = dir; }
}
