package com.smartkitchen.controller;

import com.smartkitchen.entity.Category;
import com.smartkitchen.entity.InventoryItem;
import com.smartkitchen.entity.Unit;
import com.smartkitchen.service.InventoryService;
import com.smartkitchen.validation.ItemFormValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(InventoryController.class)
@Import(ItemFormValidator.class)
class InventoryControllerExtrasTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService service;

    @Test
    void shoppingListPageLoads() throws Exception {
        mockMvc.perform(get("/inventory/shopping-list"))
                .andExpect(status().isOk())
                .andExpect(view().name("inventory/shopping-list"));
    }

    @Test
    void exportReturnsCsvDownload() throws Exception {
        mockMvc.perform(get("/inventory/export.csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"kitchen-inventory.csv\""));
    }

    @Test
    void adjustCallsServiceAndRedirects() throws Exception {
        mockMvc.perform(post("/inventory/1/adjust").param("delta", "-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory/1"));
        verify(service).adjustQuantity(eq(1L), any(BigDecimal.class));
    }

    @Test
    void adjustBelowZeroShowsErrorMessage() throws Exception {
        doThrow(new IllegalArgumentException("Quantity must be a number that is zero or more."))
                .when(service).adjustQuantity(eq(1L), any());

        mockMvc.perform(post("/inventory/1/adjust").param("delta", "-5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void addingDuplicateNameShowsWarning() throws Exception {
        InventoryItem saved = new InventoryItem("Milk", Category.DAIRY, new BigDecimal("2"), Unit.LITRE,
                new BigDecimal("1"), null, null, null);
        ReflectionTestUtils.setField(saved, "id", 7L);
        when(service.nameExists("Milk")).thenReturn(true);
        when(service.create(any())).thenReturn(saved);

        mockMvc.perform(post("/inventory")
                        .param("name", "Milk")
                        .param("category", "DAIRY")
                        .param("quantity", "2")
                        .param("unit", "LITRE")
                        .param("minimumStock", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory/7"))
                .andExpect(flash().attributeExists("warningMessage"));
    }
}
