package uk.gov.companieshouse.limitedpartnershipsapi.model.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.POSTAL_CODE_MAX_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.UK_COUNTRIES;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.UK_POSTCODE_LETTERS_NOT_MAINLAND;

public class UkPostcodeValidator implements ConstraintValidator<UkPostcode, AddressDto> {


    public boolean isValid(AddressDto addressDto, ConstraintValidatorContext context) {
        if (addressDto.getCountry() != null && UK_COUNTRIES.contains(addressDto.getCountry())) {
            if (addressDto.getPostalCode() == null || addressDto.getPostalCode().isEmpty()) {
                addConstraintViolation(context, "Postcode must not be null");

                return false;
            }

            if (addressDto.getPostalCode().length() > POSTAL_CODE_MAX_SIZE) {
                addConstraintViolation(context, "Postcode must be less than " + POSTAL_CODE_MAX_SIZE);

                return false;
            }

            var postcodePattern = "^[A-Za-z]{1,2}\\d[A-Za-z\\d]? ?\\d[A-Za-z]{2}$";
            if (!addressDto.getPostalCode().matches(postcodePattern)) {
                addConstraintViolation(context, "Invalid postcode format");

                return false;
            }

            String postcodePrefix = addressDto.getPostalCode().substring(0, 2).toUpperCase();
            if (UK_POSTCODE_LETTERS_NOT_MAINLAND.contains(postcodePrefix)) {
                addConstraintViolation(context, "Must be UK mainland postcode");

                return false;
            }
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("postalCode")
                .addConstraintViolation();
    }
}
