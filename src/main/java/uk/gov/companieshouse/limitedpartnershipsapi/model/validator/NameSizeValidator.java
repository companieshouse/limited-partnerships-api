package uk.gov.companieshouse.limitedpartnershipsapi.model.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipPatchDto;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LONG_MAX_SIZE;

public class NameSizeValidator implements ConstraintValidator<NameSize, Object> {
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object instanceof DataDto dto) {
            return isSizeCorrect(dto.getPartnershipName(), dto.getNameEnding());
        } else if (object instanceof LimitedPartnershipPatchDto dto) {
            String nameEnding = dto.getNameEnding() != null ? dto.getNameEnding().getDescription() : "";
            return isSizeCorrect(dto.getPartnershipName(), nameEnding);
        } else {
            throw new IllegalArgumentException("@NameSize only applies to DataDto or LimitedPartnershipPatchDto object");
        }
    }

    private boolean isSizeCorrect(String partnershipName, String nameEnding) {
        if (partnershipName == null && nameEnding == null) {
            return true;
        }

        var name = String.format("%s %s", partnershipName, nameEnding);
        return name.length() <= LONG_MAX_SIZE;
    }
}