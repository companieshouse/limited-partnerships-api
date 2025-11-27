package uk.gov.companieshouse.limitedpartnershipsapi.service.validator;

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
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.ContributionSubTypes;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class LimitedPartnerValidator extends PartnerValidator {

    private static final String CLASS_NAME = LimitedPartnerDataDto.class.getName();

    @Autowired
    public LimitedPartnerValidator(Validator validator, ValidationStatus validationStatus, CompanyService companyService
    ) {

        super(validator, validationStatus, companyService);
    }

    public List<ValidationStatusError> validateFull(LimitedPartnerDto limitedPartnerDto, Transaction transaction) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();

        checkFieldConstraints(limitedPartnerDto, transaction, errorsList);

        var dataDto = limitedPartnerDto.getData();
        if (dataDto.isLegalEntity()) {
            if (dataDto.getPrincipalOfficeAddress() == null) {
                errorsList.add(validationStatus.createValidationStatusError("Principal office address is required", PartnerDataDto.PRINCIPAL_OFFICE_ADDRESS_FIELD));
            }
        } else {
            if (dataDto.getUsualResidentialAddress() == null) {
                errorsList.add(validationStatus.createValidationStatusError("Usual residential address is required", PartnerDataDto.USUAL_RESIDENTIAL_ADDRESS_FIELD));
            }
        }

        return errorsList;
    }

    public void validatePartial(LimitedPartnerDto limitedPartnerDto, Transaction transaction) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
        var methodParameter = new MethodParameter(LimitedPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(limitedPartnerDto, LimitedPartnerDataDto.class.getName());

        dtoValidation(CLASS_NAME, limitedPartnerDto, bindingResult);

        var limitedPartnerDataDto = limitedPartnerDto.getData();

        if (limitedPartnerDataDto.isLegalEntity()) {
            checkNotNullLegalEntity(CLASS_NAME, limitedPartnerDataDto, bindingResult);
            validateCapitalContributions(limitedPartnerDataDto, transaction, bindingResult);
        } else if (limitedPartnerDataDto.getForename() != null || limitedPartnerDataDto.getSurname() != null) {
            checkNotNullPerson(CLASS_NAME, limitedPartnerDataDto, bindingResult);
            isSecondNationalityDifferent(CLASS_NAME, limitedPartnerDataDto, bindingResult);
            validateCapitalContributions(limitedPartnerDataDto, transaction, bindingResult);
        } else {
            addError(CLASS_NAME, "", "Some fields are missing", bindingResult);
        }

        checkNotNullDateEffectiveFrom(CLASS_NAME, limitedPartnerDto, transaction, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    public void validateRemove(LimitedPartnerDto limitedPartnerDto, Transaction transaction) throws ServiceException, NoSuchMethodException, MethodArgumentNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(limitedPartnerDto, GeneralPartnerDataDto.class.getName());

        dtoValidation(CLASS_NAME, limitedPartnerDto, bindingResult);
        validateCeaseDate(CLASS_NAME, transaction, limitedPartnerDto, bindingResult);

        if (bindingResult.hasErrors()) {
            var methodParameter = new MethodParameter(LimitedPartnerDataDto.class.getConstructor(), -1);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    private void validateCapitalContributions(LimitedPartnerDataDto limitedPartnerDataDto, Transaction transaction, BindingResult bindingResult) throws ServiceException {
        if (!FilingMode.REGISTRATION.getDescription().equals(transaction.getFilingMode())) {
            return;
        }

        PartnershipType partnershipType = limitedPartnerDataDto.getPartnershipType();

        String contributionCurrencyValue = limitedPartnerDataDto.getContributionCurrencyValue();
        Currency contributionCurrencyType = limitedPartnerDataDto.getContributionCurrencyType();
        List<ContributionSubTypes> contributionSubTypes = limitedPartnerDataDto.getContributionSubTypes();
        boolean hasContributionSubTypes = contributionSubTypes != null && !contributionSubTypes.isEmpty();

        if (partnershipType == PartnershipType.PFLP || partnershipType == PartnershipType.SPFLP) {
            validatePrivateFundPartnershipContributions(contributionCurrencyValue, contributionCurrencyType, hasContributionSubTypes, bindingResult);
        } else {
            validateStandardPartnershipContributions(contributionCurrencyValue, contributionCurrencyType, hasContributionSubTypes, bindingResult);
        }
    }

    private void validatePrivateFundPartnershipContributions(String contributionCurrencyValue, Currency contributionCurrencyType, boolean hasContributionSubTypes, BindingResult bindingResult) {
        if (contributionCurrencyValue != null) {
            addError(CLASS_NAME, LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD, "Private fund partnerships cannot have a contribution currency value", bindingResult);
        }
        if (contributionCurrencyType != null) {
            addError(CLASS_NAME, LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD, "Private fund partnerships cannot have a contribution currency type", bindingResult);
        }
        if (hasContributionSubTypes) {
            addError(CLASS_NAME, LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD, "Private fund partnerships cannot have a contribution", bindingResult);
        }
    }

    private void validateStandardPartnershipContributions(String contributionCurrencyValue, Currency contributionCurrencyType, boolean hasContributionSubTypes, BindingResult bindingResult) {
        if (contributionCurrencyValue == null || contributionCurrencyValue.isBlank() || contributionCurrencyValueIsZero(contributionCurrencyValue)) {
            addError(CLASS_NAME, LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD, "Contribution currency value is required", bindingResult);
        }

        if (contributionCurrencyType == null) {
            addError(CLASS_NAME, LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD, "Contribution currency type is required", bindingResult);
        }

        if (!hasContributionSubTypes) {
            addError(CLASS_NAME, LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD, "At least one contribution type must be selected", bindingResult);
        }
    }

    private boolean contributionCurrencyValueIsZero(String contributionCurrencyValue) {
        try {
            BigDecimal fomattedValue = new BigDecimal(contributionCurrencyValue);
            return BigDecimal.ZERO.compareTo(fomattedValue) == 0;
        } catch (NumberFormatException e) {
            ApiLogger.errorContext(contributionCurrencyValue, "Unexpected currency contribution value string format error", e);
            return false;
        }
    }

    public void validateUpdate(LimitedPartnerDto limitedPartnerDto, LimitedPartnerDataDto limitedPartnerChangesDataDto, Transaction transaction) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
        var methodParameter = new MethodParameter(LimitedPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(limitedPartnerDto, LimitedPartnerDataDto.class.getName());

        dtoValidation(CLASS_NAME, limitedPartnerDto, bindingResult);

        isSecondNationalityDifferent(CLASS_NAME, limitedPartnerDto.getData(), bindingResult);

        validateDateEffectiveFrom(CLASS_NAME, transaction, limitedPartnerDto, bindingResult);

        if (limitedPartnerChangesDataDto.getContributionCurrencyValue() != null || limitedPartnerChangesDataDto.getContributionCurrencyType() != null || limitedPartnerChangesDataDto.getContributionSubTypes() != null) {
            validateCapitalContributions(limitedPartnerChangesDataDto, transaction, bindingResult);
        }

        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    private void checkFieldConstraints(LimitedPartnerDto limitedPartnerDto, Transaction transaction, List<ValidationStatusError> errorsList)
            throws ServiceException {
        try {
            validatePartial(limitedPartnerDto, transaction);
            validateRemove(limitedPartnerDto, transaction);
        } catch (MethodArgumentNotValidException e) {
            validationStatus.convertFieldErrorsToValidationStatusErrors(e.getBindingResult(), errorsList);
        } catch (NoSuchMethodException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}

