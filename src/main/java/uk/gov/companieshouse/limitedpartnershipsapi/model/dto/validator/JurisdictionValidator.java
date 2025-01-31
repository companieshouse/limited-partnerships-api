package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class JurisdictionValidator implements ConstraintValidator<Jurisdiction, uk.gov.companieshouse.limitedpartnershipsapi.model.dto.Jurisdiction> {
    public boolean isValid(uk.gov.companieshouse.limitedpartnershipsapi.model.dto.Jurisdiction jurisdiction, ConstraintValidatorContext context) {
        if (jurisdiction == null) {
            return true;
        }

        return !uk.gov.companieshouse.limitedpartnershipsapi.model.dto.Jurisdiction.UNKNOWN.equals(jurisdiction);
    }
}