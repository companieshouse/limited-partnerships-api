package uk.gov.companieshouse.limitedpartnershipsapi.service;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class LimitedPartnerValidator extends PartnerValidator {
    private static final String CLASS_NAME = LimitedPartnerDataDto.class.getName();

    @Autowired
    public LimitedPartnerValidator(Validator validator) {
        super(validator);
    }

    public List<ValidationStatusError> validateFull(LimitedPartnerDto limitedPartnerDto) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();

        checkFieldConstraints(limitedPartnerDto, errorsList);

        var dataDto = limitedPartnerDto.getData();
        if (dataDto.isLegalEntity()) {
            if (dataDto.getPrincipalOfficeAddress() == null) {
                errorsList.add(createValidationStatusError("Principal office address is required", PartnerDataDto.PRINCIPAL_OFFICE_ADDRESS_FIELD));
            }
        } else {
            if (dataDto.getUsualResidentialAddress() == null) {
                errorsList.add(createValidationStatusError("Usual residential address is required", PartnerDataDto.USUAL_RESIDENTIAL_ADDRESS_FIELD));
            }
        }

        return errorsList;
    }

    public void validatePartial(LimitedPartnerDto limitedPartnerDto) throws NoSuchMethodException, MethodArgumentNotValidException {
        var methodParameter = new MethodParameter(LimitedPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(limitedPartnerDto, LimitedPartnerDataDto.class.getName());

        dtoValidation(CLASS_NAME, limitedPartnerDto, bindingResult);

        var limitedPartnerDataDto = limitedPartnerDto.getData();

        if (limitedPartnerDataDto.isLegalEntity()) {
            checkNotNullLegalEntity(CLASS_NAME, limitedPartnerDataDto, bindingResult);
        } else if (limitedPartnerDataDto.getForename() != null || limitedPartnerDataDto.getSurname() != null) {
            checkNotNullPerson(CLASS_NAME, limitedPartnerDataDto, bindingResult);

            checkContributionSubTypesNotNullOrEmpty(limitedPartnerDataDto, bindingResult);

            isSecondNationalityDifferent(CLASS_NAME, limitedPartnerDataDto, bindingResult);
        } else {
            addError(CLASS_NAME, "", "Some fields are missing", bindingResult);
        }

        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    private void checkContributionSubTypesNotNullOrEmpty(LimitedPartnerDataDto limitedPartnerDataDto, BindingResult bindingResult) {
        if (limitedPartnerDataDto.getContributionSubTypes() == null || limitedPartnerDataDto.getContributionSubTypes().isEmpty()) {
            addError(CLASS_NAME, "data.contributionSubTypes", "Contribution sub types is required", bindingResult);
        }
    }

    public void validateUpdate(LimitedPartnerDto limitedPartnerDto) throws NoSuchMethodException, MethodArgumentNotValidException {
        var methodParameter = new MethodParameter(LimitedPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(limitedPartnerDto, LimitedPartnerDataDto.class.getName());

        dtoValidation(CLASS_NAME, limitedPartnerDto, bindingResult);

        isSecondNationalityDifferent(CLASS_NAME, limitedPartnerDto.getData(), bindingResult);

        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    private void checkFieldConstraints(LimitedPartnerDto limitedPartnerDto, List<ValidationStatusError> errorsList)
            throws ServiceException {
        try {
            validatePartial(limitedPartnerDto);
        } catch (MethodArgumentNotValidException e) {
            convertFieldErrorsToValidationStatusErrors(e.getBindingResult(), errorsList);
        } catch (NoSuchMethodException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}

