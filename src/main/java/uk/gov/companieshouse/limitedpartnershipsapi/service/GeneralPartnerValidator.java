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
            addError("legal_entity_register_name", "Legal Entity Register Name is required", bindingResult);
        }

        if (generalPartnerDataDto.getLegalForm() == null) {
            addError("legal_form", "Legal Form is required", bindingResult);
        }

        if (generalPartnerDataDto.getGoverningLaw() == null) {
            addError("governing_law", "Governing Law is required", bindingResult);
        }

        if (generalPartnerDataDto.getLegalEntityRegistrationLocation() == null) {
            addError("legal_entity_registration_location", "Legal Entity Registration Location is required", bindingResult);
        }

        if (generalPartnerDataDto.getCountry() == null) {
            addError("country", "Country is required", bindingResult);
        }

        if (generalPartnerDataDto.getRegisteredCompanyNumber() == null) {
            addError("registered_company_number", "Registered Company Number is required", bindingResult);
        }
    }

    private void checkNotNullPerson(GeneralPartnerDataDto generalPartnerDataDto,
                                    BindingResult bindingResult) {
        if (generalPartnerDataDto.getForename() == null) {
            addError("forename", "Forename is required", bindingResult);
        }

        if (generalPartnerDataDto.getSurname() == null) {
            addError("surname", "Surname is required", bindingResult);
        }

        if (generalPartnerDataDto.getDateOfBirth() == null) {
            addError("date_of_birth", "Date of birth is required", bindingResult);
        }

        if (generalPartnerDataDto.getNationality1() == null) {
            addError("nationality1", "Nationality1 is required", bindingResult);
        }
    }

    private void isSecondNationalityDifferent(GeneralPartnerDto generalPartnerDto, BindingResult bindingResult) {
        String nationality1 = generalPartnerDto.getData().getNationality1();
        String nationality2 = generalPartnerDto.getData().getNationality2();

        if (nationality1 != null && nationality2 != null && nationality1.equals(nationality2)) {
            addError("nationality2", "Second nationality must be different from the first", bindingResult);
        }
    }

    private void addError(String legal_entity_register_name, String defaultMessage, BindingResult bindingResult) {
        var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), legal_entity_register_name, defaultMessage);
        bindingResult.addError(fieldError);
    }
}
