package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.AddressDto;

public class AddressValidator implements ConstraintValidator<ValidAddress, AddressDto> {
    public boolean isValid(AddressDto address, ConstraintValidatorContext context) {
        if (address == null) {
            return true;
        }

        if (address.getAddressLine1() != null || address.getCountry() != null || address.getLocality() != null || address.getPremises() != null) {
            return address.getAddressLine1() != null && address.getCountry() != null && address.getLocality() != null && address.getPremises() != null;
        }

        return true;
    }
}