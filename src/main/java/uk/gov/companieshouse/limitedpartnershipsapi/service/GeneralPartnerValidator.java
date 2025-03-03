package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDto;

@Component
public class GeneralPartnerValidator {

    public void isValid(GeneralPartnerDto generalPartnerDto) throws NoSuchMethodException, MethodArgumentNotValidException {
        var methodParameter = new MethodParameter(GeneralPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(generalPartnerDto, GeneralPartnerDataDto.class.getName());

        var generalPartnerDataDto = generalPartnerDto.getData();

        if (generalPartnerDataDto.getLegalEntityRegisterName() != null || generalPartnerDataDto.getLegalForm() != null) {
            checkNotNullLegalEntity(generalPartnerDataDto, bindingResult);
        } else if (generalPartnerDataDto.getForename() != null || generalPartnerDataDto.getSurname() != null) {
            checkNotNullPerson(generalPartnerDataDto, bindingResult);
            isSecondNationalityDifferent(generalPartnerDto, bindingResult);
        } else {
            addError("", "Some fields are missing", bindingResult);
        }

        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    private void checkNotNullLegalEntity(GeneralPartnerDataDto generalPartnerDataDto,
                                         BindingResult bindingResult) {
        if (generalPartnerDataDto.getLegalEntityRegisterName() == null) {
            addError(GeneralPartnerDataDto.LEGAL_ENTITY_REGISTER_NAME_FIELD, "Legal Entity Register Name is required", bindingResult);
        }

        if (generalPartnerDataDto.getLegalForm() == null) {
            addError(GeneralPartnerDataDto.LEGAL_FORM_FIELD, "Legal Form is required", bindingResult);
        }

        if (generalPartnerDataDto.getGoverningLaw() == null) {
            addError(GeneralPartnerDataDto.GOVERNING_LAW_FIELD, "Governing Law is required", bindingResult);
        }

        if (generalPartnerDataDto.getLegalEntityRegistrationLocation() == null) {
            addError(GeneralPartnerDataDto.LEGAL_ENTITY_REGISTRATION_LOCATION_FIELD, "Legal Entity Registration Location is required", bindingResult);
        }

        if (generalPartnerDataDto.getCountry() == null) {
            addError(GeneralPartnerDataDto.COUNTRY_FIELD, "Country is required", bindingResult);
        }

        if (generalPartnerDataDto.getRegisteredCompanyNumber() == null) {
            addError(GeneralPartnerDataDto.REGISTERED_COMPANY_NUMBER_FIELD, "Registered Company Number is required", bindingResult);
        }
    }

    private void checkNotNullPerson(GeneralPartnerDataDto generalPartnerDataDto,
                                    BindingResult bindingResult) {
        if (generalPartnerDataDto.getForename() == null) {
            addError(GeneralPartnerDataDto.FORENAME_FIELD, "Forename is required", bindingResult);
        }

        if (generalPartnerDataDto.getSurname() == null) {
            addError(GeneralPartnerDataDto.SURNAME_FIELD, "Surname is required", bindingResult);
        }

        if (generalPartnerDataDto.getDateOfBirth() == null) {
            addError(GeneralPartnerDataDto.DATE_OF_BIRTH_FIELD, "Date of birth is required", bindingResult);
        }

        if (generalPartnerDataDto.getNationality1() == null) {
            addError(GeneralPartnerDataDto.NATIONALITY1_FIELD, "Nationality1 is required", bindingResult);
        }
    }

    private void isSecondNationalityDifferent(GeneralPartnerDto generalPartnerDto, BindingResult bindingResult) {
        String nationality1 = generalPartnerDto.getData().getNationality1();
        String nationality2 = generalPartnerDto.getData().getNationality2();

        if (nationality1 != null && nationality2 != null && nationality1.equals(nationality2)) {
            addError(GeneralPartnerDataDto.NATIONALITY2_FIELD, "Second nationality must be different from the first", bindingResult);
        }
    }

    private void addError(String fieldName, String defaultMessage, BindingResult bindingResult) {
        var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), fieldName, defaultMessage);
        bindingResult.addError(fieldError);
    }
}
