package uk.gov.companieshouse.limitedpartnershipsapi.validator.partner;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.dto.PartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.dto.PartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.ValidationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class PartnerValidationStatus {

	protected Validator validator;
	protected ValidationStatus validationStatus;
	protected CompanyService companyService;

	public static final String NATIONALITY_1_IS_REQUIRED = "Nationality1 is required";

	@Autowired
	protected PartnerValidationStatus(Validator validator, ValidationStatus validationStatus, CompanyService companyService) {
		this.validator = validator;
		this.validationStatus = validationStatus;
		this.companyService = companyService;
	}

	protected void checkNotNullPerson(PartnerDataDto partnerDataDto, List<ValidationStatusError> errorsList) {
		checkNotNullName(partnerDataDto, errorsList);
		checkFieldNotNull(partnerDataDto.getDateOfBirth(), PartnerDataDto.DATE_OF_BIRTH_FIELD, "Date of birth is required", errorsList);
		checkFieldNotNull(partnerDataDto.getNationality1(), PartnerDataDto.NATIONALITY1_FIELD, NATIONALITY_1_IS_REQUIRED, errorsList);
	}

	protected void checkNotNullName(PartnerDataDto partnerDataDto, List<ValidationStatusError> errorsList) {
		checkFieldNotNull(partnerDataDto.getForename(), PartnerDataDto.FORENAME_FIELD, "Forename is required", errorsList);
		checkFieldNotNull(partnerDataDto.getSurname(), PartnerDataDto.SURNAME_FIELD, "Surname is required", errorsList);
	}

	protected void isSecondNationalityDifferent(PartnerDataDto partnerDataDto, List<ValidationStatusError> errorsList) {
		String nationality1 = partnerDataDto.getNationality1();
		String nationality2 = partnerDataDto.getNationality2();

		if (nationality1 != null && nationality1.equals(nationality2)) {
			errorsList.add(validationStatus.createValidationStatusError("Second nationality must be different from the first", PartnerDataDto.NATIONALITY2_FIELD));
		}
	}

	protected void checkNotNullLegalEntity(PartnerDataDto partnerDataDto, List<ValidationStatusError> errorsList) {
		checkFieldNotNull(partnerDataDto.getLegalEntityName(), PartnerDataDto.LEGAL_ENTITY_NAME_FIELD, "Legal Entity Name is required", errorsList);
		checkFieldNotNull(partnerDataDto.getLegalForm(), PartnerDataDto.LEGAL_FORM_FIELD, "Legal Form is required", errorsList);
		checkFieldNotNull(partnerDataDto.getGoverningLaw(), PartnerDataDto.GOVERNING_LAW_FIELD, "Governing Law is required", errorsList);
		checkFieldNotNull(partnerDataDto.getLegalEntityRegistrationLocation(), PartnerDataDto.LEGAL_ENTITY_REGISTRATION_LOCATION_FIELD, "Legal Entity Registration Location is required", errorsList);
	}

	protected void handleLegalEntityRegisterNameAndNumberOptionality(PartnerDataDto dataDto, List<ValidationStatusError> errorsList) {
		var enteredOnRegister = dataDto.getEnteredOnRegister();
		var registerName = dataDto.getLegalEntityRegisterName();
		var companyNumber = dataDto.getRegisteredCompanyNumber();

		if (enteredOnRegister == null) {
			errorsList.add(validationStatus.createValidationStatusError("Entered on register is required", PartnerDataDto.ENTERED_ON_REGISTER_FIELD));
		} else if (enteredOnRegister) {
			checkFieldNotNull(registerName, PartnerDataDto.LEGAL_ENTITY_REGISTER_NAME_FIELD, "Legal Entity Register Name is required when entered on register is true", errorsList);
			checkFieldNotNull(companyNumber, PartnerDataDto.REGISTERED_COMPANY_NUMBER_FIELD, "Registered Company Number is required when entered on register is true", errorsList);
		}
	}

	protected void checkNotNullDateEffectiveFrom(String className, PartnerDto partnerDto, Transaction transaction, List<ValidationStatusError> errorsList) throws ServiceException {
		if (FilingMode.DEFAULT.getDescription().equals(transaction.getFilingMode()) && PartnerKind.isAddPartnerKind(partnerDto.getData().getKind())) {
			if (partnerDto.getData().getDateEffectiveFrom() == null) {
				errorsList.add(validationStatus.createValidationStatusError("Partner date effective from is required", "data.dateEffectiveFrom"));
			}

			validateDateEffectiveFrom(className, transaction, partnerDto, errorsList);
		}
	}

	protected void validateDateEffectiveFrom(String className, Transaction transaction, PartnerDto partnerDto, List<ValidationStatusError> errorsList) throws ServiceException {
		if (partnerDto.getData().getDateEffectiveFrom() != null) {
			CompanyProfileApi companyProfileApi = companyService.getCompanyProfile(transaction.getCompanyNumber());

			LocalDate dateEffectiveFrom = partnerDto.getData().getDateEffectiveFrom();

			String partner = Objects.equals(className, GeneralPartnerDto.class.getName()) ? "general partner" : "limited partner";
			String pronoun = partnerDto.getData().isLegalEntity() ? "it" : "they";

			if (dateEffectiveFrom.isBefore(companyProfileApi.getDateOfCreation())) {
				errorsList.add(validationStatus.createValidationStatusError(String.format("Date %s became a %s must be the same as or after limited partnership's registration date", pronoun, partner), "data.dateEffectiveFrom"));
			}
		}
	}

	protected void validateCeaseDate(Transaction transaction, PartnerDto partnerDto, List<ValidationStatusError> errorsList) throws ServiceException {
		CompanyProfileApi companyProfileApi = companyService.getCompanyProfile(transaction.getCompanyNumber());

		LocalDate ceaseDate = partnerDto.getData().getCeaseDate();

		if (ceaseDate != null) {
			if (ceaseDate.isBefore(companyProfileApi.getDateOfCreation())) {
				errorsList.add(validationStatus.createValidationStatusError("Partner cease date cannot be before the incorporation date", "data.ceaseDate"));
			}

			LocalDate dateOfBirth = partnerDto.getData().getDateOfBirth();
			if (dateOfBirth != null && ceaseDate.isBefore(dateOfBirth)) {
				errorsList.add(validationStatus.createValidationStatusError("Partner cease date cannot be before the date of birth", "data.ceaseDate"));
			}
		}
	}

	protected void validateDateOfUpdate(Transaction transaction, PartnerDto partnerDto, List<ValidationStatusError> errorsList) throws ServiceException {
		if (partnerDto.getData().getDateOfUpdate() != null) {
			CompanyProfileApi companyProfileApi = companyService.getCompanyProfile(transaction.getCompanyNumber());

			LocalDate dateOfUpdate = partnerDto.getData().getDateOfUpdate();

			if (dateOfUpdate.isBefore(companyProfileApi.getDateOfCreation())) {
				errorsList.add(validationStatus.createValidationStatusError("Limited partnership date of update cannot be before the incorporation date", "data.dateOfUpdate"));
			}
		}
	}

	protected void checkFieldNotNull(Object value, String fieldName, String errorMessage, List<ValidationStatusError> errorsList) {
		if (value == null) {
			errorsList.add(validationStatus.createValidationStatusError(errorMessage, fieldName));
		}
	}

	protected void dtoValidation(PartnerDto partnerDto, List<ValidationStatusError> errorsList) {
		Set<ConstraintViolation<PartnerDto>> violations = validator.validate(
				partnerDto);

		if (!violations.isEmpty()) {
			violations.forEach(violation ->
					errorsList.add(validationStatus.createValidationStatusError(violation.getMessage(), violation.getPropertyPath().toString()))
			);
		}
	}
}
