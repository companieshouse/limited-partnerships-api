package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.enums.ContributionSubTypes;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.enums.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.dto.PartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.PartnerValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.ValidationStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class LimitedPartnerValidationStatus extends PartnerValidationStatus {

	private static final String CLASS_NAME = LimitedPartnerDataDto.class.getName();

	@Autowired
	public LimitedPartnerValidationStatus(Validator validator, ValidationStatus validationStatus, CompanyService companyService
	) {
		super(validator, validationStatus, companyService);
		this.validationStatus = validationStatus;
	}

	public List<ValidationStatusError> validate(LimitedPartnerDto limitedPartnerDto, Transaction transaction, boolean isRemoveOrUpdate) throws ServiceException {
		List<ValidationStatusError> errorsList = new ArrayList<>();

		if (PartnerKind.isUpdateLimitedPartnerKind(limitedPartnerDto.getData().getKind())) {
			validateUpdate(limitedPartnerDto, transaction, errorsList);
		} else if (PartnerKind.isRemoveLimitedPartnerKind(limitedPartnerDto.getData().getKind())) {
			validateRemove(limitedPartnerDto, transaction, errorsList);
		} else {
			validatePartial(limitedPartnerDto, transaction, errorsList);
		}

		if (isRemoveOrUpdate) {
			return errorsList;
		}

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

	public void validatePartial(LimitedPartnerDto limitedPartnerDto, Transaction transaction, List<ValidationStatusError> errorsList) throws ServiceException {
		dtoValidation(limitedPartnerDto, errorsList);

		var limitedPartnerDataDto = limitedPartnerDto.getData();

		if (limitedPartnerDataDto.isLegalEntity()) {
			checkNotNullLegalEntity(limitedPartnerDataDto, errorsList);
			handleLegalEntityRegisterNameAndNumberOptionality(limitedPartnerDataDto, errorsList);
			validateCapitalContributions(limitedPartnerDataDto, transaction, errorsList);
		} else if (limitedPartnerDataDto.getForename() != null || limitedPartnerDataDto.getSurname() != null) {
			checkNotNullPerson(limitedPartnerDataDto, errorsList);
			isSecondNationalityDifferent(limitedPartnerDataDto, errorsList);
			validateCapitalContributions(limitedPartnerDataDto, transaction, errorsList);
		} else {
			errorsList.add(validationStatus.createValidationStatusError("Some fields are missing", ""));
		}

		checkNotNullDateEffectiveFrom(CLASS_NAME, limitedPartnerDto, transaction, errorsList);
	}

	public void validateRemove(LimitedPartnerDto limitedPartnerDto, Transaction transaction, List<ValidationStatusError> errorsList) throws ServiceException {
		dtoValidation(limitedPartnerDto, errorsList);

		if (!limitedPartnerDto.getData().isLegalEntity()) {
			checkNotNullName(limitedPartnerDto.getData(), errorsList);
		}

		validateCeaseDate(transaction, limitedPartnerDto, errorsList);
	}

	private void validateCapitalContributions(LimitedPartnerDataDto limitedPartnerDataDto, Transaction transaction, List<ValidationStatusError> errorsList) {
		if (!FilingMode.REGISTRATION.getDescription().equals(transaction.getFilingMode())) {
			return;
		}

		PartnershipType partnershipType = limitedPartnerDataDto.getPartnershipType();

		String contributionCurrencyValue = limitedPartnerDataDto.getContributionCurrencyValue();
		Currency contributionCurrencyType = limitedPartnerDataDto.getContributionCurrencyType();
		List<ContributionSubTypes> contributionSubTypes = limitedPartnerDataDto.getContributionSubTypes();
		boolean hasContributionSubTypes = contributionSubTypes != null && !contributionSubTypes.isEmpty();

		if (partnershipType == PartnershipType.PFLP || partnershipType == PartnershipType.SPFLP) {
			validatePrivateFundPartnershipContributions(contributionCurrencyValue, contributionCurrencyType, hasContributionSubTypes, errorsList);
		} else {
			validateStandardPartnershipContributions(contributionCurrencyValue, contributionCurrencyType, hasContributionSubTypes, errorsList);
		}
	}

	private void validatePrivateFundPartnershipContributions(String contributionCurrencyValue, Currency contributionCurrencyType, boolean hasContributionSubTypes, List<ValidationStatusError> errorsList) {
		if (contributionCurrencyValue != null) {
			errorsList.add(validationStatus.createValidationStatusError("Private fund partnerships cannot have a contribution currency value", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD));

		}
		if (contributionCurrencyType != null) {
			errorsList.add(validationStatus.createValidationStatusError("Private fund partnerships cannot have a contribution currency type", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD));
		}
		if (hasContributionSubTypes) {
			errorsList.add(validationStatus.createValidationStatusError("Private fund partnerships cannot have a contribution", LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD));
		}
	}

	private void validateStandardPartnershipContributions(String contributionCurrencyValue, Currency contributionCurrencyType, boolean hasContributionSubTypes, List<ValidationStatusError> errorsList) {
		if (contributionCurrencyValue == null || contributionCurrencyValue.isBlank() || contributionCurrencyValueIsZero(contributionCurrencyValue)) {
			errorsList.add(validationStatus.createValidationStatusError("Contribution currency value is required", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD));
		}

		if (contributionCurrencyType == null) {
			errorsList.add(validationStatus.createValidationStatusError("Contribution currency type is required", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD));
		}

		if (!hasContributionSubTypes) {
			errorsList.add(validationStatus.createValidationStatusError("At least one contribution type must be selected", LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD));
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

	public void validateUpdate(LimitedPartnerDto limitedPartnerDto, Transaction transaction, List<ValidationStatusError> errorsList) throws ServiceException {
		dtoValidation(limitedPartnerDto, errorsList);

		if (limitedPartnerDto.getData().isLegalEntity() || PartnerKind.isLegalEntityKind(limitedPartnerDto.getData().getKind())) {
			checkNotNullLegalEntity(limitedPartnerDto.getData(), errorsList);
			handleLegalEntityRegisterNameAndNumberOptionality(limitedPartnerDto.getData(), errorsList);
		} else {
			checkNotNullName(limitedPartnerDto.getData(), errorsList);
			checkFieldNotNull(limitedPartnerDto.getData().getNationality1(), PartnerDataDto.NATIONALITY1_FIELD, NATIONALITY_1_IS_REQUIRED, errorsList);
			isSecondNationalityDifferent(limitedPartnerDto.getData(), errorsList);
		}

		validateDateEffectiveFrom(CLASS_NAME, transaction, limitedPartnerDto, errorsList);

		validateDateOfUpdate(transaction, limitedPartnerDto, errorsList);

		if (limitedPartnerDto.getData().getContributionCurrencyValue() != null || limitedPartnerDto.getData().getContributionCurrencyType() != null || limitedPartnerDto.getData().getContributionSubTypes() != null) {
			validateCapitalContributions(limitedPartnerDto.getData(), transaction, errorsList);
		}
	}
}

