package uk.gov.companieshouse.limitedpartnershipsapi.model.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;

import java.util.Objects;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LONG_MAX_SIZE;

public class NameSizeValidator implements ConstraintValidator<NameSize, Object> {

    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (Objects.requireNonNull(object) instanceof DataDto dto) {
            return isSizeCorrect(dto.getPartnershipName(), dto.getNameEnding());
        }

        throw new IllegalArgumentException("@NameSize only applies to DataDto or limitedPartnershipDataDto object");
    }

    private boolean isSizeCorrect(String partnershipName, String nameEnding) {
        if (partnershipName == null && nameEnding == null) {
            return true;
        }

        var name = String.format("%s %s", partnershipName, nameEnding);
        return name.length() <= LONG_MAX_SIZE;
    }
}