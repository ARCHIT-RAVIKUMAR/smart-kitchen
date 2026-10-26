package com.smartkitchen.dto;

/** One bar of the "items by category" chart on the dashboard. */
public class CategoryCount {

    private final String label;
    private final long count;
    private final int percent;

    public CategoryCount(String label, long count, int percent) {
        this.label = label;
        this.count = count;
        this.percent = percent;
    }

    public String getLabel() { return label; }
    public long getCount() { return count; }
    public int getPercent() { return percent; }
}
