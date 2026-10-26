package com.smartkitchen.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PagesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void helpAndHealthPagesWork() throws Exception {
        mockMvc.perform(get("/help")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Help")));
        mockMvc.perform(get("/health")).andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void shoppingListAndCsvIncludeNewItem() throws Exception {
        mockMvc.perform(post("/inventory")
                        .param("name", "Csv Test Oil")
                        .param("category", "OTHER")
                        .param("quantity", "0")
                        .param("unit", "LITRE")
                        .param("minimumStock", "1"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/inventory/shopping-list"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Csv Test Oil")));

        mockMvc.perform(get("/inventory/export.csv").param("q", "csv test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(containsString("Csv Test Oil")));
    }

    @Test
    void duplicateAndQuantityAdjustFlowWorks() throws Exception {
        mockMvc.perform(post("/inventory")
                        .param("name", "Adjust Test Rice")
                        .param("category", "GRAINS_CEREALS")
                        .param("quantity", "3")
                        .param("unit", "KG")
                        .param("minimumStock", "1"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/inventory").param("q", "adjust test"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("3 kg")));
    }
}
