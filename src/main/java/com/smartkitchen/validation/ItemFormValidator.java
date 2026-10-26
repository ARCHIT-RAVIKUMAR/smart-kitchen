package com.smartkitchen.validation;

import com.smartkitchen.dto.InventoryItemForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/** Extra rule that annotations cannot express: expiry date must not be before purchase date. */
@Component
public class ItemFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return InventoryItemForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        InventoryItemForm form = (InventoryItemForm) target;
        if (form.getPurchaseDate() != null && form.getExpiryDate() != null
                && form.getExpiryDate().isBefore(form.getPurchaseDate())) {
            errors.rejectValue("expiryDate", "expiry.beforePurchase",
                    "Expiry date cannot be before the purchase date.");
        }
    }
}
