package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {
    String message() default "Enum must be valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}