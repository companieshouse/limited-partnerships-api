package uk.gov.companieshouse.limitedpartnershipsapi.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class LimitedPartnershipValidator {

    private final Validator validator;

    @Autowired
    public LimitedPartnershipValidator(Validator validator) {
        this.validator = validator;
    }

    public List<ValidationStatusError> validate(LimitedPartnershipDto limitedPartnershipDto) {
        Set<ConstraintViolation<LimitedPartnershipDto>> violations = validator.validate(limitedPartnershipDto);

        List<ValidationStatusError> errorsList = new ArrayList<>();
        violations.forEach(v ->
                errorsList.add(createValidationStatusError(v.getMessage(), v.getPropertyPath().toString())));

        final var dataDto = limitedPartnershipDto.getData();

        checkCommonFields(dataDto, errorsList);
        checkPartnershipTypeSpecificFields(dataDto, errorsList);

        return errorsList;
    }

    private void checkCommonFields(DataDto dataDto, List<ValidationStatusError> errorsList) {
        if (dataDto.getEmail() == null) {
            errorsList.add(createValidationStatusError("Email is required", "data.email"));
        }

        if (dataDto.getJurisdiction() == null) {
            errorsList.add(createValidationStatusError("Jurisdiction is required", "data.jurisdiction"));
        }

        if (dataDto.getRegisteredOfficeAddress() == null) {
            errorsList.add(createValidationStatusError("Registered office address is required",
                    "data.registeredOfficeAddress"));
        }

        if (dataDto.getPrincipalPlaceOfBusinessAddress() == null) {
            errorsList.add(createValidationStatusError("Principal place of business address is required",
                    "data.principalPlaceOfBusinessAddress"));
        }

        if (!Boolean.TRUE.equals(dataDto.getLawfulPurposeStatementChecked())) {
            errorsList.add(createValidationStatusError("Lawful purpose statement checked is required",
                    "data.lawfulPurposeStatementChecked"));
        }
    }

    private void checkPartnershipTypeSpecificFields(DataDto dataDto, List<ValidationStatusError> errorsList) {
        if (PartnershipType.PFLP.equals(dataDto.getPartnershipType())
                || PartnershipType.SPFLP.equals(dataDto.getPartnershipType())) {
            if (dataDto.getTerm() != null) {
                errorsList.add(createValidationStatusError("Term is not required", "data.term"));
            }

            if (dataDto.getSicCodes() != null) {
                errorsList.add(createValidationStatusError("SIC codes are not required", "data.sicCodes"));
            }
        } else {
            if (dataDto.getTerm() == null) {
                errorsList.add(createValidationStatusError("Term is required", "data.term"));
            }

            if (dataDto.getSicCodes() == null || dataDto.getSicCodes().isEmpty()) {
                errorsList.add(createValidationStatusError("SIC codes are required", "data.sicCodes"));
            }
        }
    }

    private ValidationStatusError createValidationStatusError(String errorMessage, String location) {
        var error = new ValidationStatusError();
        error.setError(errorMessage);
        error.setLocation(location);
        return error;
    }
}
