package uk.gov.companieshouse.limitedpartnershipsapi.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LONG_MAX_SIZE;

@Constraint(validatedBy = NameSizeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NameSize {
    String message() default "Max length 'partnership name + name ending' is " + LONG_MAX_SIZE + " characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}