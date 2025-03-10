package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Term;

public class EnumValidator implements ConstraintValidator<ValidEnum, Enum> {
    public boolean isValid(Enum enumeration, ConstraintValidatorContext context) {
        return switch (enumeration) {
            case null -> true;
            case Jurisdiction jurisdiction -> !Jurisdiction.UNKNOWN.equals(enumeration);
            case Term term -> !Term.UNKNOWN.equals(enumeration);
            case Nationality nationality -> !Nationality.UNKNOWN.equals(enumeration);
            case Country country -> !Country.UNKNOWN.equals(enumeration);
            case IncorporationKind incorporationKind -> !IncorporationKind.UNKNOWN.equals(enumeration);
            default -> false;
        };
    }
}