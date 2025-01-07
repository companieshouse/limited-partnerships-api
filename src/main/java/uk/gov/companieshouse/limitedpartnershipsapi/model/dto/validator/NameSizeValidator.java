package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;

public class NameSizeValidator implements ConstraintValidator<NameSize, DataDto> {
    public boolean isValid(DataDto object, ConstraintValidatorContext context) {
        if (!(object instanceof DataDto)) {
            throw new IllegalArgumentException("@NameSize only applies to DataDto object");
        }

        var name = String.format("%s %s", object.getPartnershipName(), object.getNameEnding());

        return name.length() <= DataDto.NAME_MAX_SIZE;
    }
}