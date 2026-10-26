package com.smartkitchen.controller;

import com.smartkitchen.dto.InventoryItemForm;
import com.smartkitchen.dto.ItemFilter;
import com.smartkitchen.entity.Category;
import com.smartkitchen.entity.ExpiryStatus;
import com.smartkitchen.entity.InventoryItem;
import com.smartkitchen.entity.StockStatus;
import com.smartkitchen.entity.Unit;
import com.smartkitchen.service.InventoryService;
import com.smartkitchen.validation.ItemFormValidator;
import jakarta.validation.Valid;
import com.smartkitchen.util.CsvUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/** Handles web requests only. All business rules are in InventoryService. */
@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService service;
    private final ItemFormValidator formValidator;

    public InventoryController(InventoryService service, ItemFormValidator formValidator) {
        this.service = service;
        this.formValidator = formValidator;
    }

    @InitBinder("itemForm")
    void initBinder(WebDataBinder binder) {
        binder.addValidators(formValidator);
    }

    // ---------------------------------------------------------------- list pages

    @GetMapping
    public String list(@ModelAttribute("filter") ItemFilter filter, Model model) {
        model.addAttribute("items", service.search(filter));
        model.addAttribute("totalCount", service.countAll());
        model.addAttribute("categories", Category.values());
        model.addAttribute("stockStatuses", StockStatus.values());
        model.addAttribute("expiryStatuses", ExpiryStatus.values());
        return "inventory/list";
    }

    @GetMapping("/low-stock")
    public String lowStock(Model model) {
        model.addAttribute("outOfStock", service.itemsWithStockStatus(StockStatus.OUT_OF_STOCK));
        model.addAttribute("lowStock", service.itemsWithStockStatus(StockStatus.LOW_STOCK));
        return "inventory/low-stock";
    }

    @GetMapping("/expiry")
    public String expiry(Model model) {
        model.addAttribute("expired", service.itemsWithExpiryStatus(ExpiryStatus.EXPIRED));
        model.addAttribute("expiringSoon", service.itemsWithExpiryStatus(ExpiryStatus.EXPIRING_SOON));
        model.addAttribute("expiringSoonDays", service.getExpiringSoonDays());
        return "inventory/expiry";
    }

    @GetMapping("/shopping-list")
    public String shoppingList(Model model) {
        model.addAttribute("items", service.getShoppingList());
        return "inventory/shopping-list";
    }

    @GetMapping("/export.csv")
    public ResponseEntity<byte[]> exportCsv(@ModelAttribute("filter") ItemFilter filter) {
        byte[] body = CsvUtil.toCsv(service.search(filter));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"kitchen-inventory.csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(body);
    }

    // ---------------------------------------------------------------- add

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("itemForm", new InventoryItemForm());
        prepareForm(model, null);
        return "inventory/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("itemForm") InventoryItemForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, null);
            return "inventory/form";
        }
        boolean duplicate = service.nameExists(form.getName());
        InventoryItem saved = service.create(form);
        redirect.addFlashAttribute("successMessage", "\"" + saved.getName() + "\" was added to your inventory.");
        if (duplicate) {
            redirect.addFlashAttribute("warningMessage",
                    "You already had an item with this name. Check that you are not tracking the same thing twice.");
        }
        return "redirect:/inventory/" + saved.getId();
    }

    // ---------------------------------------------------------------- view

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("view", service.getView(id));
        return "inventory/details";
    }

    // ---------------------------------------------------------------- edit

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("itemForm", service.getForm(id));
        prepareForm(model, id);
        return "inventory/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("itemForm") InventoryItemForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, id);
            return "inventory/form";
        }
        InventoryItem saved = service.update(id, form);
        redirect.addFlashAttribute("successMessage", "\"" + saved.getName() + "\" was updated.");
        return "redirect:/inventory/" + id;
    }

    // ---------------------------------------------------------------- quantity + delete

    @PostMapping("/{id}/quantity")
    public String updateQuantity(@PathVariable Long id,
                                 @RequestParam(required = false) BigDecimal quantity,
                                 RedirectAttributes redirect) {
        try {
            service.updateQuantity(id, quantity);
            redirect.addFlashAttribute("successMessage", "Quantity updated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/inventory/" + id;
    }

    @PostMapping("/{id}/adjust")
    public String adjust(@PathVariable Long id, @RequestParam BigDecimal delta, RedirectAttributes redirect) {
        try {
            service.adjustQuantity(id, delta);
            redirect.addFlashAttribute("successMessage", "Quantity adjusted.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/inventory/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        String name = service.delete(id);
        redirect.addFlashAttribute("successMessage", "\"" + name + "\" was deleted.");
        return "redirect:/inventory";
    }

    // ---------------------------------------------------------------- helper

    private void prepareForm(Model model, Long itemId) {
        model.addAttribute("itemId", itemId);
        model.addAttribute("pageTitle", itemId == null ? "Add Item" : "Edit Item");
        model.addAttribute("categories", Category.values());
        model.addAttribute("units", Unit.values());
    }
}
