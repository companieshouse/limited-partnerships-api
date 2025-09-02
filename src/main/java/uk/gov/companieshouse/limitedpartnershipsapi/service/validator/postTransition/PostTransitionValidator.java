package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.postTransition;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class PostTransitionValidator {

    private final Validator validator;
    private final ValidationStatus validationStatus;

    private final Map<String, PostTransitionStrategy> strategyMap = Map.of(
            PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS.getDescription(), new UpdateRegisteredOfficeAddress(),
            PartnershipKind.UPDATE_PARTNERSHIP_NAME.getDescription(), new UpdatePartnershipName()
    );

    @Autowired
    public PostTransitionValidator(Validator validator, ValidationStatus validationStatus) {
        this.validator = validator;
        this.validationStatus = validationStatus;
    }

    public List<ValidationStatusError> validate(LimitedPartnershipDto limitedPartnershipDto) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();

        Set<ConstraintViolation<LimitedPartnershipDto>> violations = validator.validate(limitedPartnershipDto);

        if (!violations.isEmpty()) {
            violations.forEach(violation ->
                    errorsList.add(validationStatus.createValidationStatusError(violation.getMessage(), violation.getPropertyPath().toString()))
            );
        }

        if (limitedPartnershipDto.getData().getDateOfUpdate() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Date of update is required",
                    "data.dateOfUpdate"));
        }

        PostTransitionStrategy strategy = strategyMap.get(limitedPartnershipDto.getData().getKind());

        if (strategy == null) {
            throw new ServiceException("No strategy found for kind: " + limitedPartnershipDto.getData().getKind());
        }

        strategy.validate(limitedPartnershipDto, errorsList, validationStatus);

        return errorsList;
    }
}
