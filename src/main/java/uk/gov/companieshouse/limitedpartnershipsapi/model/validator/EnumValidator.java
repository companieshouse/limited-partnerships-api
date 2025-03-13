package uk.gov.companieshouse.limitedpartnershipsapi.model.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;

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