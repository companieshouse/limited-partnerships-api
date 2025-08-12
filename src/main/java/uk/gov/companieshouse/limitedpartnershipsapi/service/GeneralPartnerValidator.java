package uk.gov.companieshouse.limitedpartnershipsapi.service;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;

import java.util.ArrayList;
import java.util.List;

@Component
public class GeneralPartnerValidator extends PartnerValidator {
    private static final String CLASS_NAME = GeneralPartnerDataDto.class.getName();

    @Autowired
    public GeneralPartnerValidator(Validator validator, CompanyService companyService) {
        super(validator, companyService);
    }

    public List<ValidationStatusError> validateFull(GeneralPartnerDto generalPartnerDto, Transaction transaction) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();

        checkFieldConstraints(generalPartnerDto, transaction, errorsList);

        var dataDto = generalPartnerDto.getData();
        if (dataDto.isLegalEntity()) {
            if (dataDto.getPrincipalOfficeAddress() == null) {
                errorsList.add(createValidationStatusError("Principal office address is required", PartnerDataDto.PRINCIPAL_OFFICE_ADDRESS_FIELD));
            }
        } else {
            if (dataDto.getUsualResidentialAddress() == null) {
                errorsList.add(createValidationStatusError("Usual residential address is required", PartnerDataDto.USUAL_RESIDENTIAL_ADDRESS_FIELD));
            }

            if (dataDto.getServiceAddress() == null) {
                errorsList.add(createValidationStatusError("Service address is required", GeneralPartnerDataDto.SERVICE_ADDRESS_FIELD));
            }
        }

        return errorsList;
    }

    public void validatePartial(GeneralPartnerDto generalPartnerDto, Transaction transaction) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
        BindingResult bindingResult = new BeanPropertyBindingResult(generalPartnerDto, GeneralPartnerDataDto.class.getName());

        dtoValidation(CLASS_NAME, generalPartnerDto, bindingResult);

        var generalPartnerDataDto = generalPartnerDto.getData();

        if (generalPartnerDataDto.isLegalEntity()) {
            checkNotNullLegalEntity(CLASS_NAME, generalPartnerDataDto, bindingResult);
        } else if (generalPartnerDataDto.getForename() != null || generalPartnerDataDto.getSurname() != null) {
            checkNotNullPerson(CLASS_NAME, generalPartnerDataDto, bindingResult);
            isSecondNationalityDifferent(CLASS_NAME, generalPartnerDataDto, bindingResult);
            if (!transaction.getFilingMode().equals(IncorporationKind.TRANSITION.getDescription())) {
                var notDisqualifiedStatementChecked = generalPartnerDataDto.getNotDisqualifiedStatementChecked();
                if (notDisqualifiedStatementChecked == null || Boolean.FALSE.equals(notDisqualifiedStatementChecked)) {
                    addError(CLASS_NAME, GeneralPartnerDataDto.NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD, "Not Disqualified Statement must be checked", bindingResult);
                }
            }

        } else {
            addError(CLASS_NAME, "", "Some fields are missing", bindingResult);
        }

        checkNotNullDateEffectiveFrom(CLASS_NAME, generalPartnerDto, transaction, bindingResult);

        if (bindingResult.hasErrors()) {
            var methodParameter = new MethodParameter(GeneralPartnerDataDto.class.getConstructor(), -1);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    public void validateUpdate(GeneralPartnerDto generalPartnerDto, Transaction transaction) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
        BindingResult bindingResult = new BeanPropertyBindingResult(generalPartnerDto, GeneralPartnerDataDto.class.getName());

        dtoValidation(CLASS_NAME, generalPartnerDto, bindingResult);

        isSecondNationalityDifferent(CLASS_NAME, generalPartnerDto.getData(), bindingResult);

        validateDateEffectiveFrom(CLASS_NAME, transaction, generalPartnerDto, bindingResult);

        if (bindingResult.hasErrors()) {
            var methodParameter = new MethodParameter(GeneralPartnerDataDto.class.getConstructor(), -1);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    private void checkFieldConstraints(GeneralPartnerDto generalPartnerDto, Transaction transaction, List<ValidationStatusError> errorsList)
            throws ServiceException {
        try {
            validatePartial(generalPartnerDto, transaction);
        } catch (MethodArgumentNotValidException e) {
            convertFieldErrorsToValidationStatusErrors(e.getBindingResult(), errorsList);
        } catch (NoSuchMethodException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}
