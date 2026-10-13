package uk.gov.companieshouse.limitedpartnershipsapi.generalpartner;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.dto.PartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.PartnerValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.ValidationStatus;

import java.util.ArrayList;
import java.util.List;

@Component
public class GeneralPartnerValidationStatus extends PartnerValidationStatus {
	private static final String CLASS_NAME = GeneralPartnerDataDto.class.getName();

	@Autowired
	public GeneralPartnerValidationStatus(Validator validator, ValidationStatus validationStatus, CompanyService companyService) {
		super(validator, validationStatus, companyService);
	}

	public List<ValidationStatusError> validate(GeneralPartnerDto generalPartnerDto, Transaction transaction, boolean isRemoveOrUpdate) throws ServiceException {
		List<ValidationStatusError> errorsList = new ArrayList<>();

		if (PartnerKind.isUpdateGeneralPartnerKind(generalPartnerDto.getData().getKind())) {
			validateUpdate(generalPartnerDto, transaction, errorsList);
		} else if (PartnerKind.isRemoveGeneralPartnerKind(generalPartnerDto.getData().getKind())) {
			validateRemove(generalPartnerDto, transaction, errorsList);
		} else {
			validatePartial(generalPartnerDto, transaction, errorsList);
		}

		if (isRemoveOrUpdate) {
			return errorsList;
		}

		var dataDto = generalPartnerDto.getData();
		if (dataDto.isLegalEntity()) {
			if (dataDto.getPrincipalOfficeAddress() == null) {
				errorsList.add(validationStatus.createValidationStatusError("Principal office address is required", PartnerDataDto.PRINCIPAL_OFFICE_ADDRESS_FIELD));
			}
		} else {
			if (dataDto.getUsualResidentialAddress() == null) {
				errorsList.add(validationStatus.createValidationStatusError("Usual residential address is required", PartnerDataDto.USUAL_RESIDENTIAL_ADDRESS_FIELD));
			}

			if (dataDto.getServiceAddress() == null) {
				errorsList.add(validationStatus.createValidationStatusError("Service address is required", GeneralPartnerDataDto.SERVICE_ADDRESS_FIELD));
			}
		}

		return errorsList;
	}

	public void validatePartial(GeneralPartnerDto generalPartnerDto, Transaction transaction, List<ValidationStatusError> errorsList) throws ServiceException {
		dtoValidation(generalPartnerDto, errorsList);

		var generalPartnerDataDto = generalPartnerDto.getData();

		if (transaction.getFilingMode().equals(FilingMode.REGISTRATION.getDescription()) ||
				(transaction.getFilingMode().equals(FilingMode.DEFAULT.getDescription()) && PartnerKind.isAddPartnerKind(generalPartnerDataDto.getKind()))
		) {
			var notDisqualifiedStatementChecked = generalPartnerDataDto.getNotDisqualifiedStatementChecked();
			if (notDisqualifiedStatementChecked == null || Boolean.FALSE.equals(notDisqualifiedStatementChecked)) {
				errorsList.add(validationStatus.createValidationStatusError("Not Disqualified Statement must be checked", GeneralPartnerDataDto.NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD));
			}
		}

		if (generalPartnerDataDto.isLegalEntity()) {
			checkNotNullLegalEntity(generalPartnerDataDto, errorsList);
			handleLegalEntityRegisterNameAndNumberOptionality(generalPartnerDataDto, errorsList);
		} else if (generalPartnerDataDto.getForename() != null || generalPartnerDataDto.getSurname() != null) {
			checkNotNullPerson(generalPartnerDataDto, errorsList);
			isSecondNationalityDifferent(generalPartnerDataDto, errorsList);
		} else {
			errorsList.add(validationStatus.createValidationStatusError("Some fields are missing", ""));
		}

		checkNotNullDateEffectiveFrom(CLASS_NAME, generalPartnerDto, transaction, errorsList);
	}

	public void validateRemove(GeneralPartnerDto generalPartnerDto, Transaction transaction, List<ValidationStatusError> errorsList) throws ServiceException {
		dtoValidation(generalPartnerDto, errorsList);

		if (!PartnerKind.isLegalEntityKind(generalPartnerDto.getData().getKind())) {
			checkNotNullName(generalPartnerDto.getData(), errorsList);
		}

		validateCeaseDate(transaction, generalPartnerDto, errorsList);
	}

	public void validateUpdate(GeneralPartnerDto generalPartnerDto, Transaction transaction, List<ValidationStatusError> errorsList) throws ServiceException {
		dtoValidation(generalPartnerDto, errorsList);

		if (generalPartnerDto.getData().isLegalEntity() || PartnerKind.isLegalEntityKind(generalPartnerDto.getData().getKind())) {
			checkNotNullLegalEntity(generalPartnerDto.getData(), errorsList);
			handleLegalEntityRegisterNameAndNumberOptionality(generalPartnerDto.getData(), errorsList);
		} else {
			checkNotNullName(generalPartnerDto.getData(), errorsList);
			checkFieldNotNull(generalPartnerDto.getData().getNationality1(), PartnerDataDto.NATIONALITY1_FIELD, NATIONALITY_1_IS_REQUIRED, errorsList);
			isSecondNationalityDifferent(generalPartnerDto.getData(), errorsList);
		}

		validateDateEffectiveFrom(CLASS_NAME, transaction, generalPartnerDto, errorsList);

		validateDateOfUpdate(transaction, generalPartnerDto, errorsList);
	}
}
