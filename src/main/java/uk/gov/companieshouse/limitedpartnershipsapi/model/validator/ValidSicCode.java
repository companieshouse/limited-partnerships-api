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
    String message() default "Sic code can only contain numeric values";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}