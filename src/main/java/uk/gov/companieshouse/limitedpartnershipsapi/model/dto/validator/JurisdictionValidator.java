package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static uk.gov.companieshouse.limitedpartnershipsapi.model.dto.Jurisdiction.UNKNOWN;

public class JurisdictionValidator implements ConstraintValidator<ValidJurisdiction, uk.gov.companieshouse.limitedpartnershipsapi.model.dto.Jurisdiction> {
    public boolean isValid(uk.gov.companieshouse.limitedpartnershipsapi.model.dto.Jurisdiction jurisdiction, ConstraintValidatorContext context) {
        if (jurisdiction == null) {
            return true;
        }

        return !UNKNOWN.equals(jurisdiction);
    }
}