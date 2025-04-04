package uk.gov.companieshouse.limitedpartnershipsapi.model.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.AddressDto;

import java.util.List;

public class UkPostcodeValidator implements ConstraintValidator<UkPostcode, AddressDto> {
    private final List<String> UK_COUNTRIES = List.of(
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

            if (addressDto.getPostalCode().length() > 15) {
                addConstraintViolation(context, "Postcode must be less than 15");

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
