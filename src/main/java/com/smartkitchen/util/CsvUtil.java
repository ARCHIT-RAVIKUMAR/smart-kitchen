package com.smartkitchen.util;

import com.smartkitchen.dto.InventoryItemView;
import com.smartkitchen.entity.InventoryItem;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Builds the CSV export. Every cell is quoted and spreadsheet formulas are neutralised. */
public final class CsvUtil {

    private CsvUtil() {
    }

    public static byte[] toCsv(List<InventoryItemView> views) {
        StringBuilder sb = new StringBuilder("\uFEFF"); // BOM so Excel reads UTF-8 correctly
        appendRow(sb, "Name", "Category", "Quantity", "Unit", "Minimum stock", "Stock status",
                "Purchase date", "Expiry date", "Expiry status", "Notes");
        for (InventoryItemView v : views) {
            InventoryItem i = v.getItem();
            appendRow(sb,
                    i.getName(),
                    i.getCategory().getLabel(),
                    i.getQuantity().toPlainString(),
                    i.getUnit().getLabel(),
                    i.getMinimumStock().toPlainString(),
                    v.getStockStatus().getLabel(),
                    i.getPurchaseDate() == null ? "" : i.getPurchaseDate().toString(),
                    i.getExpiryDate() == null ? "" : i.getExpiryDate().toString(),
                    v.getExpiryStatus().getLabel(),
                    i.getNotes());
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static String escape(String value) {
        String text = value == null ? "" : value;
        if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text; // stops Excel running it as a formula
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private static void appendRow(StringBuilder sb, String... cells) {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(cells[i]));
        }
        sb.append("\r\n");
    }
}
