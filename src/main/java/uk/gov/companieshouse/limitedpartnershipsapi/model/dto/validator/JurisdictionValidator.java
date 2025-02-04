package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Jurisdiction;

import static uk.gov.companieshouse.limitedpartnershipsapi.model.Jurisdiction.UNKNOWN;

public class JurisdictionValidator implements ConstraintValidator<ValidJurisdiction, Jurisdiction> {
    public boolean isValid(Jurisdiction jurisdiction, ConstraintValidatorContext context) {
        if (jurisdiction == null) {
            return true;
        }

        return !UNKNOWN.equals(jurisdiction);
    }
}