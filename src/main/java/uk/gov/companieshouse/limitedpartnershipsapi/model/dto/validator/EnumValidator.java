package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Term;

public class EnumValidator implements ConstraintValidator<ValidEnum, Enum> {
    public boolean isValid(Enum enumeration, ConstraintValidatorContext context) {
        return switch (enumeration) {
            case null -> true;
            case Jurisdiction jurisdiction -> !Jurisdiction.UNKNOWN.equals(enumeration);
            case Term term -> !Term.UNKNOWN.equals(enumeration);
            default -> false;
        };
    }
}