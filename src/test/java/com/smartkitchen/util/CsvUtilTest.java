package com.smartkitchen.util;

import com.smartkitchen.dto.InventoryItemView;
import com.smartkitchen.entity.Category;
import com.smartkitchen.entity.ExpiryStatus;
import com.smartkitchen.entity.InventoryItem;
import com.smartkitchen.entity.StockStatus;
import com.smartkitchen.entity.Unit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvUtilTest {

    @Test
    void escapeQuotesAndDoublesInnerQuotes() {
        assertEquals("\"a\"\"b\"", CsvUtil.escape("a\"b"));
        assertEquals("\"\"", CsvUtil.escape(null));
    }

    @Test
    void escapeNeutralisesSpreadsheetFormulas() {
        assertEquals("\"'=1+1\"", CsvUtil.escape("=1+1"));
        assertEquals("\"'@SUM(A1)\"", CsvUtil.escape("@SUM(A1)"));
    }

    @Test
    void csvHasHeaderAndOneRowPerItem() {
        InventoryItem item = new InventoryItem("Rice, Basmati", Category.GRAINS_CEREALS, new BigDecimal("2.50"),
                Unit.KG, new BigDecimal("1"), null, null, "Note");
        InventoryItemView view = new InventoryItemView(item, StockStatus.IN_STOCK, ExpiryStatus.NO_EXPIRY,
                null, BigDecimal.ZERO);

        String csv = new String(CsvUtil.toCsv(List.of(view)), StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"Name\",\"Category\""));
        assertTrue(csv.contains("\"Rice, Basmati\""));
        assertTrue(csv.contains("\"2.50\""));
        assertEquals(2, csv.split("\r\n").length);
    }
}
