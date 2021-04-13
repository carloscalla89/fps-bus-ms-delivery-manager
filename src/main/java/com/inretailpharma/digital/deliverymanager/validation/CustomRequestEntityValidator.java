package com.inretailpharma.digital.deliverymanager.validation;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import lombok.Data;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Data
public class CustomRequestEntityValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return OrderDto.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "ecommercePurchaseId", "field.required",
                "The ecommercePurchaseId should not be null");


    }
}
