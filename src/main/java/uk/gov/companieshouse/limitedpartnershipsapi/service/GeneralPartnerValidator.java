package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDto;

@Component
public class GeneralPartnerValidator {

    public void isValid(GeneralPartnerDto generalPartnerDto) throws NoSuchMethodException, MethodArgumentNotValidException {
        var methodParameter = new MethodParameter(GeneralPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(generalPartnerDto, GeneralPartnerDataDto.class.getName());

        GeneralPartnerDataDto generalPartnerDataDto = generalPartnerDto.getData();

        if (generalPartnerDataDto.getLegalEntityRegisterName() != null || generalPartnerDataDto.getLegalForm() != null) {
            checkNotNullLegalEntity(generalPartnerDataDto, bindingResult);
        } else if (generalPartnerDataDto.getForename() != null || generalPartnerDataDto.getSurname() != null) {
            checkNotNullPerson(generalPartnerDataDto, bindingResult);

            if (generalPartnerDto.getData().getNationality1() != null) {
                checkNationalities(generalPartnerDto, bindingResult);
            }
        } else {
            var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), "", "Some fields are missing");
            bindingResult.addError(fieldError);
        }

        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    private void checkNotNullLegalEntity(GeneralPartnerDataDto generalPartnerDataDto,
                                         BindingResult bindingResult) {
        if (generalPartnerDataDto.getLegalEntityRegisterName() == null) {
            var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), "legal_entity_register_name", "Legal Entity Register Name must not be null");
            bindingResult.addError(fieldError);
        }

        if (generalPartnerDataDto.getLegalForm() == null) {
            var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), "legal_form", "Legal Form must not be null");
            bindingResult.addError(fieldError);
        }

        if (generalPartnerDataDto.getGoverningLaw() == null) {
            var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), "governing_law", "Governing Law must not be null");
            bindingResult.addError(fieldError);
        }

        if (generalPartnerDataDto.getLegalEntityRegistrationLocation() == null) {
            var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), "legal_entity_registration_location", "Legal Entity Registration Location must not be null");
            bindingResult.addError(fieldError);
        }

        if (generalPartnerDataDto.getCountry() == null) {
            var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), "country", "Country must not be null");
            bindingResult.addError(fieldError);
        }

        if (generalPartnerDataDto.getRegisteredCompanyNumber() == null) {
            var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), "registered_company_number", "Registered Company Number must not be null");
            bindingResult.addError(fieldError);
        }
    }

    private void checkNotNullPerson(GeneralPartnerDataDto generalPartnerDataDto,
                                    BindingResult bindingResult) {
        if (generalPartnerDataDto.getForename() == null) {
            var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), "forename", "forename must not be null");
            bindingResult.addError(fieldError);
        }

        if (generalPartnerDataDto.getSurname() == null) {
            var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), "surname", "surname must not be null");
            bindingResult.addError(fieldError);
        }

        if (generalPartnerDataDto.getDateOfBirth() == null) {
            var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), "date_of_birth", "Date of birth must not be null");
            bindingResult.addError(fieldError);
        }

        if (generalPartnerDataDto.getNationality1() == null) {
            var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), "nationality1", "Nationality1 must not be null");
            bindingResult.addError(fieldError);
        }
    }

    private void checkNationalities(GeneralPartnerDto generalPartnerDto, BindingResult bindingResult) {
        if (!isSecondNationalityDifferent(generalPartnerDto.getData().getNationality1(), generalPartnerDto.getData().getNationality2())) {
            var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), "Nationality", "Second nationality must be different from the first");
            bindingResult.addError(fieldError);
        }
    }

    private boolean isSecondNationalityDifferent(String nationality1, String nationality2) {
        if (StringUtils.isBlank(nationality2) || nationality2.equals(Nationality.UNKNOWN.getDescription())) {
            return !(StringUtils.isBlank(nationality1) || nationality1.equals(Nationality.UNKNOWN.getDescription()));
        }
        return !nationality1.equals(nationality2);
    }
}
