package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AddressValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAddress {
    String message() default "The address_line_1, country, locality, postal_code, premises fields are required.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}