package com.smartkitchen.controller;

import com.smartkitchen.dto.Alert;
import com.smartkitchen.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class DashboardController {

    private static final int MAX_ALERTS_ON_DASHBOARD = 8;

    private final InventoryService service;

    public DashboardController(InventoryService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        List<Alert> alerts = service.getAlerts();
        model.addAttribute("summary", service.getDashboardSummary());
        model.addAttribute("alerts", alerts.subList(0, Math.min(alerts.size(), MAX_ALERTS_ON_DASHBOARD)));
        model.addAttribute("totalAlerts", alerts.size());
        model.addAttribute("activities", service.getRecentActivity());
        model.addAttribute("categories", service.getCategoryBreakdown());
        model.addAttribute("expiringSoonDays", service.getExpiringSoonDays());
        return "dashboard";
    }

    @GetMapping("/help")
    public String help(Model model) {
        model.addAttribute("expiringSoonDays", service.getExpiringSoonDays());
        return "help";
    }

    /** Lightweight check used by the hosting platform (no database call). */
    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "OK";
    }
}
