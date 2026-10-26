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

/** Full flow: web layer -> service -> repository -> database (H2 in tests). */
@SpringBootTest
@AutoConfigureMockMvc
class InventoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addItemThenSeeItInListAndDashboard() throws Exception {
        mockMvc.perform(post("/inventory")
                        .param("name", "Integration Rice")
                        .param("category", "GRAINS_CEREALS")
                        .param("quantity", "0")
                        .param("unit", "KG")
                        .param("minimumStock", "2"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/inventory").param("q", "integration"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Integration Rice")))
                .andExpect(content().string(containsString("Out of Stock")));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dashboard")));

        mockMvc.perform(get("/inventory/low-stock"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Integration Rice")));

        mockMvc.perform(get("/inventory/expiry"))
                .andExpect(status().isOk());
    }

    @Test
    void unknownItemReturns404() throws Exception {
        mockMvc.perform(get("/inventory/987654"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("987654")));
    }

    @Test
    void unknownUrlReturns404Page() throws Exception {
        mockMvc.perform(get("/does-not-exist"))
                .andExpect(status().isNotFound());
    }
}
