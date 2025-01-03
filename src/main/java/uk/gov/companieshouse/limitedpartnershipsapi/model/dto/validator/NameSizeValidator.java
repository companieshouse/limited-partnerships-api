package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;

public class NameSizeValidator implements ConstraintValidator<NameSize, DataDto> {
    public void initialize(NameSize nameSize) {
    }

    public boolean isValid(DataDto object, ConstraintValidatorContext context) {
        if (!(object instanceof DataDto)) {
            throw new IllegalArgumentException("@NameSize only applies to DataDto object");
        }

        DataDto dataDto = (DataDto) object;

        return dataDto.getPartnershipName().length() + dataDto.getNameEnding().length() <= 160;

    }
}