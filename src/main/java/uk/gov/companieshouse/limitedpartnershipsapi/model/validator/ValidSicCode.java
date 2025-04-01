package uk.gov.companieshouse.limitedpartnershipsapi.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = SicCodeValidator.class)
@Target({ElementType.TYPE_USE, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSicCode {
    String message() default "Sic code must be 5 numeric characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}