package uk.gov.companieshouse.limitedpartnershipsapi.service;

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
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class LimitedPartnershipValidator {
    private static final String CLASS_NAME = LimitedPartnershipDataDto.class.getName();

    private final Validator validator;

    @Autowired
    public LimitedPartnershipValidator(Validator validator) {
        this.validator = validator;
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
        BindingResult bindingResult = new BeanPropertyBindingResult(limitedPartnershipDto, LimitedPartnershipDataDto.class.getName());

        dtoValidation(limitedPartnershipDto, bindingResult);

        if (incorporationKind != null) {
            checkJourneySpecificFields(limitedPartnershipDto.getData(), incorporationKind, bindingResult);
        }

        if (bindingResult.hasErrors()) {
            var methodParameter = new MethodParameter(LimitedPartnershipDataDto.class.getConstructor(), -1);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    public List<ValidationStatusError> validatePostTransition(LimitedPartnershipDto limitedPartnershipDto) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();

        if (limitedPartnershipDto.getData().getDateOfUpdate() == null) {
            errorsList.add(createValidationStatusError("Date of update is required",
                    "data.dateOfUpdate"));
        }

        if (limitedPartnershipDto.getData().getKind().equals(PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS.getDescription())) {

            checkFieldConstraints(limitedPartnershipDto, null, errorsList);

            if (limitedPartnershipDto.getData().getRegisteredOfficeAddress() == null) {
                errorsList.add(createValidationStatusError("Registered office address is required",
                        "data.registeredOfficeAddress"));
            }
        }

        return errorsList;
    }

    private void checkCommonFields(LimitedPartnershipDataDto limitedPartnershipDataDto, IncorporationKind incorporationKind, List<ValidationStatusError> errorsList) {
        if (limitedPartnershipDataDto.getEmail() == null) {
            errorsList.add(createValidationStatusError("Email is required", "data.email"));
        }

        if (limitedPartnershipDataDto.getJurisdiction() == null) {
            errorsList.add(createValidationStatusError("Jurisdiction is required", "data.jurisdiction"));
        }

        if (limitedPartnershipDataDto.getRegisteredOfficeAddress() == null) {
            errorsList.add(createValidationStatusError("Registered office address is required",
                    "data.registeredOfficeAddress"));
        }

        if (limitedPartnershipDataDto.getPrincipalPlaceOfBusinessAddress() == null && incorporationKind.equals(IncorporationKind.REGISTRATION)) {
            errorsList.add(createValidationStatusError("Principal place of business address is required",
                    "data.principalPlaceOfBusinessAddress"));
        }

        if ((limitedPartnershipDataDto.getLawfulPurposeStatementChecked() == null || limitedPartnershipDataDto.getLawfulPurposeStatementChecked() == Boolean.FALSE) && incorporationKind.equals(IncorporationKind.REGISTRATION)) {
            errorsList.add(createValidationStatusError("Lawful purpose statement checked is required",
                    "data.lawfulPurposeStatementChecked"));
        }
    }

    private void checkPartnershipTypeSpecificFields(LimitedPartnershipDataDto limitedPartnershipDataDto, IncorporationKind incorporationKind, List<ValidationStatusError> errorsList) {
        if (PartnershipType.PFLP.equals(limitedPartnershipDataDto.getPartnershipType())
                || PartnershipType.SPFLP.equals(limitedPartnershipDataDto.getPartnershipType())) {
            if (limitedPartnershipDataDto.getTerm() != null) {
                errorsList.add(createValidationStatusError("Term is not required", "data.term"));
            }

            if (limitedPartnershipDataDto.getSicCodes() != null) {
                errorsList.add(createValidationStatusError("SIC codes are not required", "data.sicCodes"));
            }
        } else {
            if (limitedPartnershipDataDto.getTerm() == null && incorporationKind.equals(IncorporationKind.REGISTRATION)) {
                errorsList.add(createValidationStatusError("Term is required", "data.term"));
            }

            if ((limitedPartnershipDataDto.getSicCodes() == null || limitedPartnershipDataDto.getSicCodes().isEmpty()) && incorporationKind.equals(IncorporationKind.REGISTRATION)) {
                errorsList.add(createValidationStatusError("SIC codes are required", "data.sicCodes"));
            }
        }
    }

    private void checkJourneySpecificFields(LimitedPartnershipDataDto limitedPartnershipDataDto, IncorporationKind incorporationKind, BindingResult bindingResult) {
        if (IncorporationKind.REGISTRATION.equals(incorporationKind)) {
            if (limitedPartnershipDataDto.getNameEnding() == null) {
                addError("data.nameEnding", "Name ending is required", bindingResult);
            }

            if (limitedPartnershipDataDto.getPartnershipName() == null) {
                addError("data.partnershipName", "Limited partnership name is required", bindingResult);
            }

            if (limitedPartnershipDataDto.getPartnershipType() == null) {
                addError("data.partnershipType", "Limited partnership type is required", bindingResult);
            }
        } else {
            if (limitedPartnershipDataDto.getPartnershipNumber() == null) {
                addError("data.partnershipNumber", "Limited partnership number is required", bindingResult);
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

    private ValidationStatusError createValidationStatusError(String errorMessage, String location) {
        var error = new ValidationStatusError();
        error.setError(errorMessage);
        error.setLocation(location);
        return error;
    }

    private void checkFieldConstraints(LimitedPartnershipDto limitedPartnershipDto, IncorporationKind incorporationKind, List<ValidationStatusError> errorsList)
            throws ServiceException {
        try {
            validatePartial(limitedPartnershipDto, incorporationKind);
        } catch (MethodArgumentNotValidException e) {
            convertFieldErrorsToValidationStatusErrors(e.getBindingResult(), errorsList);
        } catch (NoSuchMethodException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    private void convertFieldErrorsToValidationStatusErrors(BindingResult bindingResult, List<ValidationStatusError> errorsList) {
        bindingResult.getFieldErrors().forEach(fe ->
                errorsList.add(createValidationStatusError(fe.getDefaultMessage(), fe.getField())));
    }
}
