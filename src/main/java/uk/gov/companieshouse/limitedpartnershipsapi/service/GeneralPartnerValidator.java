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

    public void checkNotNull(GeneralPartnerDto generalPartnerDto) throws NoSuchMethodException, MethodArgumentNotValidException {
        var methodParameter = new MethodParameter(GeneralPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(generalPartnerDto, GeneralPartnerDataDto.class.getName());

        GeneralPartnerDataDto generalPartnerDataDto = generalPartnerDto.getData();

        if (generalPartnerDataDto.getLegalEntityRegisterName() != null || generalPartnerDataDto.getLegalForm() != null) {
            checkNotNullLegalEntity(generalPartnerDataDto, bindingResult, methodParameter);
        }
    }

    private void checkNotNullLegalEntity(GeneralPartnerDataDto generalPartnerDataDto,
                                         BindingResult bindingResult,
                                         MethodParameter methodParameter) throws MethodArgumentNotValidException {
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

        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }
}
