package com.smartkitchen.controller;

import com.smartkitchen.entity.Category;
import com.smartkitchen.entity.InventoryItem;
import com.smartkitchen.entity.Unit;
import com.smartkitchen.exception.ItemNotFoundException;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(InventoryController.class)
@Import(ItemFormValidator.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService service;

    @Test
    void listPageLoads() throws Exception {
        when(service.search(any())).thenReturn(List.of());

        mockMvc.perform(get("/inventory"))
                .andExpect(status().isOk())
                .andExpect(view().name("inventory/list"));
    }

    @Test
    void newFormLoads() throws Exception {
        mockMvc.perform(get("/inventory/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("inventory/form"));
    }

    @Test
    void missingItemShowsFriendly404Page() throws Exception {
        when(service.getView(99L)).thenThrow(new ItemNotFoundException(99L));

        mockMvc.perform(get("/inventory/99"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void invalidIdShowsFriendlyBadRequestPage() throws Exception {
        mockMvc.perform(get("/inventory/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error"));
    }

    @Test
    void invalidFormIsRedisplayedWithErrors() throws Exception {
        mockMvc.perform(post("/inventory")
                        .param("name", "")
                        .param("category", "DAIRY")
                        .param("quantity", "-5")
                        .param("unit", "KG")
                        .param("minimumStock", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("inventory/form"))
                .andExpect(model().attributeHasFieldErrors("itemForm", "name", "quantity"));

        verify(service, never()).create(any());
    }

    @Test
    void validFormCreatesItemAndRedirectsToDetails() throws Exception {
        InventoryItem saved = new InventoryItem("Milk", Category.DAIRY, new BigDecimal("2"), Unit.LITRE,
                new BigDecimal("1"), null, null, null);
        ReflectionTestUtils.setField(saved, "id", 1L);
        when(service.create(any())).thenReturn(saved);

        mockMvc.perform(post("/inventory")
                        .param("name", "Milk")
                        .param("category", "DAIRY")
                        .param("quantity", "2")
                        .param("unit", "LITRE")
                        .param("minimumStock", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory/1"));
    }

    @Test
    void negativeQuantityUpdateIsRejectedWithMessage() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Quantity must be a number that is zero or more."))
                .when(service).updateQuantity(eq(1L), any());

        mockMvc.perform(post("/inventory/1/quantity").param("quantity", "-3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory/1"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .flash().attributeExists("errorMessage"));
    }

    @Test
    void deleteRedirectsToList() throws Exception {
        when(service.delete(1L)).thenReturn("Milk");

        mockMvc.perform(post("/inventory/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory"));
    }
}
