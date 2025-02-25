package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.NationalityList;

public class NationalityValidator implements ConstraintValidator<Nationality, Object> {

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        if (object instanceof GeneralPartnerDataDto dto) {
            String nationality1 = dto.getNationality1();
            String nationality2 = dto.getNationality2();
            if (StringUtils.isNotBlank(nationality2)) {
                return !nationality1.equals(nationality2) &&
                        EnumUtils.isValidEnum(NationalityList.class, nationality1.toUpperCase()) &&
                        EnumUtils.isValidEnum(NationalityList.class, nationality2.toUpperCase());
            }
            return EnumUtils.isValidEnum(NationalityList.class, nationality1.toUpperCase());
        } else {
            throw new IllegalArgumentException("@Nationality only applies to the GeneralPartnerDto object");
        }
    }
}
