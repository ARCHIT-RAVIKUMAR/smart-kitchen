package com.smartkitchen.validation;

import com.smartkitchen.dto.InventoryItemForm;
import com.smartkitchen.entity.Category;
import com.smartkitchen.entity.InventoryItem;
import com.smartkitchen.entity.Unit;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryItemFormValidationTest {

    private final Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ItemFormValidator dateValidator = new ItemFormValidator();

    private InventoryItemForm validForm() {
        InventoryItemForm f = new InventoryItemForm();
        f.setName("Rice");
        f.setCategory(Category.GRAINS_CEREALS);
        f.setQuantity(new BigDecimal("2"));
        f.setUnit(Unit.KG);
        f.setMinimumStock(new BigDecimal("1"));
        return f;
    }

    private boolean hasViolationOn(Set<ConstraintViolation<InventoryItemForm>> violations, String property) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(property));
    }

    @Test
    void validFormHasNoViolations() {
        assertTrue(beanValidator.validate(validForm()).isEmpty());
    }

    @Test
    void blankNameIsRejected() {
        InventoryItemForm f = validForm();
        f.setName("   ");
        assertTrue(hasViolationOn(beanValidator.validate(f), "name"));
    }

    @Test
    void negativeQuantityIsRejected() {
        InventoryItemForm f = validForm();
        f.setQuantity(new BigDecimal("-1"));
        assertTrue(hasViolationOn(beanValidator.validate(f), "quantity"));
    }

    @Test
    void negativeMinimumStockIsRejected() {
        InventoryItemForm f = validForm();
        f.setMinimumStock(new BigDecimal("-0.5"));
        assertTrue(hasViolationOn(beanValidator.validate(f), "minimumStock"));
    }

    @Test
    void missingCategoryAndUnitAreRejected() {
        InventoryItemForm f = validForm();
        f.setCategory(null);
        f.setUnit(null);
        Set<ConstraintViolation<InventoryItemForm>> violations = beanValidator.validate(f);
        assertTrue(hasViolationOn(violations, "category"));
        assertTrue(hasViolationOn(violations, "unit"));
    }

    @Test
    void expiryBeforePurchaseIsRejected() {
        InventoryItemForm f = validForm();
        f.setPurchaseDate(LocalDate.of(2026, 10, 10));
        f.setExpiryDate(LocalDate.of(2026, 10, 9));
        Errors errors = new BeanPropertyBindingResult(f, "itemForm");
        dateValidator.validate(f, errors);
        assertTrue(errors.hasFieldErrors("expiryDate"));
    }

    @Test
    void itemWithoutExpiryDateIsAccepted() {
        InventoryItemForm f = validForm();
        f.setPurchaseDate(LocalDate.of(2026, 10, 10));
        f.setExpiryDate(null);
        Errors errors = new BeanPropertyBindingResult(f, "itemForm");
        dateValidator.validate(f, errors);
        assertFalse(errors.hasErrors());
    }

    @Test
    void entityRefusesNegativeQuantity() {
        InventoryItem item = new InventoryItem("Rice", Category.OTHER, BigDecimal.ONE, Unit.KG,
                BigDecimal.ONE, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> item.setQuantity(new BigDecimal("-1")));
    }
}
