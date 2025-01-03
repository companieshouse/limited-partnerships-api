package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NameSizeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NameSize {
    String message() default "Max length 'data.partnership_name + data.name_ending' is 160 characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}