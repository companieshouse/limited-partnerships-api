package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NationalityValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Nationality {

    String message() default "Check nationality is valid and any second nationality is different from the first";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
