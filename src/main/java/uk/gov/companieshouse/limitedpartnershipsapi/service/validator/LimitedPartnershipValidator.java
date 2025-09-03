package uk.gov.companieshouse.limitedpartnershipsapi.service.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class LimitedPartnershipValidator {
    private static final String CLASS_NAME = DataDto.class.getName();

    private final Validator validator;
    private final ValidationStatus validationStatus;

    @Autowired
    public LimitedPartnershipValidator(Validator validator, ValidationStatus validationStatus) {
        this.validator = validator;
        this.validationStatus = validationStatus;
    }

    public List<ValidationStatusError> validateFull(LimitedPartnershipDto limitedPartnershipDto,
                                                    IncorporationKind incorporationKind) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();

        checkFieldConstraints(limitedPartnershipDto, incorporationKind, errorsList);

        final var dataDto = limitedPartnershipDto.getData();

        checkCommonFields(dataDto, incorporationKind, errorsList);
        checkPartnershipTypeSpecificFields(dataDto, incorporationKind, errorsList);

        return errorsList;
    }

    public void validatePartial(LimitedPartnershipDto limitedPartnershipDto,
                                IncorporationKind incorporationKind)
            throws NoSuchMethodException, MethodArgumentNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(limitedPartnershipDto, DataDto.class.getName());

        dtoValidation(limitedPartnershipDto, bindingResult);

        if (incorporationKind != null) {
            checkJourneySpecificFields(limitedPartnershipDto.getData(), incorporationKind, bindingResult);
        }

        if (bindingResult.hasErrors()) {
            var methodParameter = new MethodParameter(DataDto.class.getConstructor(), -1);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    private void checkCommonFields(DataDto dataDto, IncorporationKind incorporationKind, List<ValidationStatusError> errorsList) {
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

        if (dataDto.getPrincipalPlaceOfBusinessAddress() == null && incorporationKind.equals(IncorporationKind.REGISTRATION)) {
            errorsList.add(validationStatus.createValidationStatusError("Principal place of business address is required",
                    "data.principalPlaceOfBusinessAddress"));
        }

        if ((dataDto.getLawfulPurposeStatementChecked() == null || dataDto.getLawfulPurposeStatementChecked() == Boolean.FALSE) && incorporationKind.equals(IncorporationKind.REGISTRATION)) {
            errorsList.add(validationStatus.createValidationStatusError("Lawful purpose statement checked is required",
                    "data.lawfulPurposeStatementChecked"));
        }
    }

    private void checkPartnershipTypeSpecificFields(DataDto dataDto, IncorporationKind incorporationKind, List<ValidationStatusError> errorsList) {
        if (PartnershipType.PFLP.equals(dataDto.getPartnershipType())
                || PartnershipType.SPFLP.equals(dataDto.getPartnershipType())) {
            if (dataDto.getTerm() != null) {
                errorsList.add(validationStatus.createValidationStatusError("Term is not required", "data.term"));
            }

            if (dataDto.getSicCodes() != null) {
                errorsList.add(validationStatus.createValidationStatusError("SIC codes are not required", "data.sicCodes"));
            }
        } else {
            if (dataDto.getTerm() == null && incorporationKind.equals(IncorporationKind.REGISTRATION)) {
                errorsList.add(validationStatus.createValidationStatusError("Term is required", "data.term"));
            }

            if ((dataDto.getSicCodes() == null || dataDto.getSicCodes().isEmpty()) && incorporationKind.equals(IncorporationKind.REGISTRATION)) {
                errorsList.add(validationStatus.createValidationStatusError("SIC codes are required", "data.sicCodes"));
            }
        }
    }

    private void checkJourneySpecificFields(DataDto dataDto, IncorporationKind incorporationKind, BindingResult bindingResult) {
        if (IncorporationKind.REGISTRATION.equals(incorporationKind)) {
            if (dataDto.getNameEnding() == null) {
                addError("data.nameEnding", "Name ending is required", bindingResult);
            }
        } else {
            if (dataDto.getPartnershipNumber() == null) {
                addError("data.partnershipNumber", "Partnership number is required", bindingResult);
            }
        }
    }

    private void dtoValidation(LimitedPartnershipDto limitedPartnershipDto, BindingResult bindingResult) {
        Set<ConstraintViolation<LimitedPartnershipDto>> violations = validator.validate(limitedPartnershipDto);

        if (!violations.isEmpty()) {
            violations.forEach(violation ->
                    addError(violation.getPropertyPath().toString(), violation.getMessage(), bindingResult)
            );
        }
    }

    private void addError(String fieldName, String defaultMessage, BindingResult bindingResult) {
        bindingResult.addError(new FieldError(CLASS_NAME, fieldName, defaultMessage));
    }

    private void checkFieldConstraints(LimitedPartnershipDto limitedPartnershipDto, IncorporationKind incorporationKind, List<ValidationStatusError> errorsList)
            throws ServiceException {
        try {
            validatePartial(limitedPartnershipDto, incorporationKind);
        } catch (MethodArgumentNotValidException e) {
            validationStatus.convertFieldErrorsToValidationStatusErrors(e.getBindingResult(), errorsList);
        } catch (NoSuchMethodException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}
