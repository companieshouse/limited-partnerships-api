package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.ValidationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode.REGISTRATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.SCOTTISH_PARTNERSHIP_TYPES;

@Component
public class LimitedPartnershipValidationStatus {
	private final Validator validator;
	private final ValidationStatus validationStatus;

	@Autowired
	public LimitedPartnershipValidationStatus(Validator validator, ValidationStatus validationStatus) {
		this.validator = validator;
		this.validationStatus = validationStatus;
	}

	public List<ValidationStatusError> validate(PartnershipDto partnershipDto, FilingMode filingMode) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
		List<ValidationStatusError> errorsList = new ArrayList<>();

		validateDtoANdJourney(partnershipDto, filingMode, errorsList);
		checkCommonFields(partnershipDto, filingMode, errorsList);
		checkPartnershipTypeSpecificFields(partnershipDto, filingMode, errorsList);
		validateHasPersonWithSignificantControl(partnershipDto, filingMode, errorsList);

		return errorsList;
	}

	private void validateDtoANdJourney(PartnershipDto partnershipDto, FilingMode filingMode, List<ValidationStatusError> errorsList) {
		dtoValidation(partnershipDto, errorsList);

		if (filingMode != null) {
			checkJourneySpecificFields(partnershipDto, filingMode, errorsList);
		}
	}

	private void dtoValidation(PartnershipDto partnershipDto, List<ValidationStatusError> errorsList) {
		Set<ConstraintViolation<PartnershipDto>> violations = validator.validate(partnershipDto);

		if (!violations.isEmpty()) {
			violations.forEach(violation ->
					errorsList.add(validationStatus.createValidationStatusError(violation.getMessage(), violation.getPropertyPath().toString()))
			);
		}
	}

	private void checkJourneySpecificFields(PartnershipDto partnershipDto, FilingMode filingMode, List<ValidationStatusError> errorsList) {
		DataDto dataDto = partnershipDto.getData();

		if (FilingMode.REGISTRATION.equals(filingMode)) {
			if (dataDto.getNameEnding() == null) {
				errorsList.add(validationStatus.createValidationStatusError("Name ending is required", "data.nameEnding"));
			}
		} else {
			if (dataDto.getPartnershipNumber() == null) {
				errorsList.add(validationStatus.createValidationStatusError("Partnership number is required", "data.partnershipNumber"));
			}
		}
	}

	private void checkCommonFields(PartnershipDto partnershipDto, FilingMode filingMode, List<ValidationStatusError> errorsList) {
		DataDto dataDto = partnershipDto.getData();

		if (dataDto.getEmail() == null) {
			errorsList.add(validationStatus.createValidationStatusError("Email is required", "data.email"));
		}

		if (dataDto.getJurisdiction() == null) {
			errorsList.add(validationStatus.createValidationStatusError("Jurisdiction is required", "data.jurisdiction"));
		}

		if (dataDto.getRegisteredOfficeAddress() == null) {
			errorsList.add(validationStatus.createValidationStatusError("Registered office address is required",
					"data.registeredOfficeAddress"));
		}

		if (dataDto.getPrincipalPlaceOfBusinessAddress() == null && filingMode.equals(FilingMode.REGISTRATION)) {
			errorsList.add(validationStatus.createValidationStatusError("Principal place of business address is required",
					"data.principalPlaceOfBusinessAddress"));
		}

		if ((dataDto.getLawfulPurposeStatementChecked() == null || dataDto.getLawfulPurposeStatementChecked() == Boolean.FALSE) && filingMode.equals(FilingMode.REGISTRATION)) {
			errorsList.add(validationStatus.createValidationStatusError("Lawful purpose statement checked is required",
					"data.lawfulPurposeStatementChecked"));
		}
	}

	private void checkPartnershipTypeSpecificFields(PartnershipDto partnershipDto, FilingMode filingMode, List<ValidationStatusError> errorsList) {
		DataDto dataDto = partnershipDto.getData();

		if (PartnershipType.PFLP.equals(dataDto.getPartnershipType())
				|| PartnershipType.SPFLP.equals(dataDto.getPartnershipType())) {
			if (dataDto.getTerm() != null) {
				errorsList.add(validationStatus.createValidationStatusError("Term is not required", "data.term"));
			}

			if (dataDto.getSicCodes() != null) {
				errorsList.add(validationStatus.createValidationStatusError("SIC codes are not required", "data.sicCodes"));
			}
		} else {
			if (dataDto.getTerm() == null && filingMode.equals(FilingMode.REGISTRATION)) {
				errorsList.add(validationStatus.createValidationStatusError("Term is required", "data.term"));
			}

			if ((dataDto.getSicCodes() == null || dataDto.getSicCodes().isEmpty()) && filingMode.equals(FilingMode.REGISTRATION)) {
				errorsList.add(validationStatus.createValidationStatusError("SIC codes are required", "data.sicCodes"));
			}
		}
	}

	private void validateHasPersonWithSignificantControl(PartnershipDto partnershipDto, FilingMode filingMode, List<ValidationStatusError> errorsList) {
		DataDto dataDto = partnershipDto.getData();

		String errorLocation = "data.hasPersonWithSignificantControl";

		if (!REGISTRATION.equals(filingMode)) {
			if (Objects.nonNull(dataDto.getHasPersonWithSignificantControl())) {
				errorsList.add(validationStatus.createValidationStatusError(
						"You can only declare whether the partnership will or will not have a person with significant control during registration",
						errorLocation));
			}
			return;
		}

		PartnershipType partnershipType = dataDto.getPartnershipType();

		if (SCOTTISH_PARTNERSHIP_TYPES.contains(partnershipType)
				&& Objects.isNull(dataDto.getHasPersonWithSignificantControl())) {
			errorsList.add(validationStatus.createValidationStatusError(
					"You must declare whether the partnership will or will not have a person with significant control",
					errorLocation));
		}
		if ((PartnershipType.LP.equals(partnershipType)
				|| PartnershipType.PFLP.equals(partnershipType))
				&& Objects.nonNull(dataDto.getHasPersonWithSignificantControl())) {
			errorsList.add(validationStatus.createValidationStatusError(
					"This type of partnership can not have a person with significant control",
					errorLocation));
		}
	}
}
