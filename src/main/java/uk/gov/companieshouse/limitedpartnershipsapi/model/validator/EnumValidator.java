package uk.gov.companieshouse.limitedpartnershipsapi.model.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;

public class EnumValidator implements ConstraintValidator<EnumValid, Enum<?>> {
    public boolean isValid(Enum enumeration, ConstraintValidatorContext context) {
        return switch (enumeration) {
            case null -> true;
            case Jurisdiction jurisdiction -> !Jurisdiction.UNKNOWN.equals(jurisdiction);
            case Term term -> !Term.UNKNOWN.equals(term);
            case Nationality nationality -> !Nationality.UNKNOWN.equals(nationality);
            case Country country -> !Country.UNKNOWN.equals(country);
            case Currency currency -> !Currency.UNKNOWN.equals(currency);
            case IncorporationKind incorporationKind -> !IncorporationKind.UNKNOWN.equals(incorporationKind);
            case PartnershipType partnershipType -> !PartnershipType.UNKNOWN.equals(partnershipType);
            case PartnershipNameEnding partnershipNameEnding -> !PartnershipNameEnding.UNKNOWN.equals(partnershipNameEnding);
            default -> false;
        };
    }
}
