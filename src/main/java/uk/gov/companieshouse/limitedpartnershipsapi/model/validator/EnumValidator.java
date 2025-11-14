package uk.gov.companieshouse.limitedpartnershipsapi.model.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.ContributionSubTypes;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;

public class EnumValidator implements ConstraintValidator<EnumValid, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        switch (value) {
            case null -> {
                return true;
            }
            case Enum<?> anEnum -> {
                return isValidEnum(anEnum);
            }
            case java.util.List<?> objects -> {
                for (Object item : objects) {
                    if (item instanceof Enum<?> && !isValidEnum((Enum<?>) item)) {
                        return false;
                    }
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private boolean isValidEnum(Enum<?> enumeration) {
        return switch (enumeration) {
            case null -> true;
            case Jurisdiction jurisdiction -> !Jurisdiction.UNKNOWN.equals(jurisdiction);
            case Term term -> !Term.UNKNOWN.equals(term);
            case Nationality nationality -> !Nationality.UNKNOWN.equals(nationality);
            case Country country -> !Country.UNKNOWN.equals(country);
            case Currency currency -> !Currency.UNKNOWN.equals(currency);
            case FilingMode filingMode -> !FilingMode.UNKNOWN.equals(filingMode);
            case PartnershipType partnershipType -> !PartnershipType.UNKNOWN.equals(partnershipType);
            case PartnershipNameEnding partnershipNameEnding ->
                    !PartnershipNameEnding.UNKNOWN.equals(partnershipNameEnding);
            case ContributionSubTypes contributionSubTypes ->
                    !ContributionSubTypes.UNKNOWN.equals(contributionSubTypes);
            default -> false;
        };
    }
}
