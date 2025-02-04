package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = JurisdictionValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidJurisdiction {
    String message() default "Jurisdiction must be valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}