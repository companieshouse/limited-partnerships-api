package uk.gov.companieshouse.limitedpartnershipsapi.model.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.AddressDto;

import java.util.List;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.POSTAL_CODE_MAX_SIZE;

public class UkPostcodeValidator implements ConstraintValidator<UkPostcode, AddressDto> {
    private static final List<String> UK_COUNTRIES = List.of(
            Country.ENGLAND.getDescription(),
            Country.WALES.getDescription(),
            Country.SCOTLAND.getDescription(),
            Country.NORTHERN_IRELAND.getDescription());

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
