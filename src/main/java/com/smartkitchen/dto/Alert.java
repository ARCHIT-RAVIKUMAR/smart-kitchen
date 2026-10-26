package com.smartkitchen.dto;

/** One in-app alert. Exactly one alert exists per item per condition. */
public class Alert {

    private final String severity; // "danger" or "warning" (Bootstrap colour names)
    private final String message;
    private final Long itemId;

    public Alert(String severity, String message, Long itemId) {
        this.severity = severity;
        this.message = message;
        this.itemId = itemId;
    }

    public String getSeverity() { return severity; }
    public String getMessage() { return message; }
    public Long getItemId() { return itemId; }
}
