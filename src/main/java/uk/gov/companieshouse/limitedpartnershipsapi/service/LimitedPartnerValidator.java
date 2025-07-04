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
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.ContributionSubTypes;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class LimitedPartnerValidator extends PartnerValidator {

    private final LimitedPartnershipService limitedPartnershipService;

    private static final String CLASS_NAME = LimitedPartnerDataDto.class.getName();

    @Autowired
    public LimitedPartnerValidator(Validator validator, CompanyService companyService, LimitedPartnershipService limitedPartnershipService) {
        super(validator, companyService);
        this.limitedPartnershipService = limitedPartnershipService;
    }

    public List<ValidationStatusError> validateFull(LimitedPartnerDto limitedPartnerDto, Transaction transaction) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();

        checkFieldConstraints(limitedPartnerDto, transaction, errorsList);

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

    private void validateCapitalContributions(LimitedPartnerDataDto limitedPartnerDataDto, Transaction transaction, BindingResult bindingResult) throws ServiceException {
        if (!IncorporationKind.REGISTRATION.getDescription().equals(transaction.getFilingMode())) {
            return;
        }

        LimitedPartnershipDto limitedPartnershipDto = limitedPartnershipService.getLimitedPartnership(transaction);
        PartnershipType partnershipType = limitedPartnershipDto.getData().getPartnershipType();

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
        if (contributionCurrencyValue == null || contributionCurrencyValue.isBlank()) {
            addError(CLASS_NAME, LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD, "Contribution currency value is required", bindingResult);
        } else if(containsInvalidCurrencyFormat(contributionCurrencyValue)) {
            addError(CLASS_NAME, LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD, "Value must be a valid decimal number", bindingResult);
        }

        if (contributionCurrencyType == null) {
            addError(CLASS_NAME, LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD, "Contribution currency type is required", bindingResult);
        }

        if (!hasContributionSubTypes) {
            addError(CLASS_NAME, LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD, "At least one contribution type must be selected", bindingResult);
        }
    }

    private boolean containsInvalidCurrencyFormat(String contributionCurrencyValue){
        try {
           BigDecimal fomattedValue = new BigDecimal(contributionCurrencyValue);
           int numberOfDeicmalPlaces =  fomattedValue.scale();
           return BigDecimal.ZERO.compareTo(fomattedValue) == 0 || numberOfDeicmalPlaces != 2;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public void validateUpdate(LimitedPartnerDto limitedPartnerDto, LimitedPartnerDataDto limitedPartnerChangesDataDto, Transaction transaction) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
        var methodParameter = new MethodParameter(LimitedPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(limitedPartnerDto, LimitedPartnerDataDto.class.getName());

        dtoValidation(CLASS_NAME, limitedPartnerDto, bindingResult);
        // TODO validate all mandatory fields are supplied in the Patch

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
        } catch (MethodArgumentNotValidException e) {
            convertFieldErrorsToValidationStatusErrors(e.getBindingResult(), errorsList);
        } catch (NoSuchMethodException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}

