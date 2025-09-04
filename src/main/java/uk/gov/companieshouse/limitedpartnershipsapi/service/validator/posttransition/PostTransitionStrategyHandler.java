package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;

@Component
public class PostTransitionStrategyHandler {

    private final Validator validator;
    private final ValidationStatus validationStatus;

    private final Map<String, PostTransitionStrategy> strategyMap;

    @Autowired
    public PostTransitionStrategyHandler(Map<String, PostTransitionStrategy> strategyMap, Validator validator, ValidationStatus validationStatus) {
        this.strategyMap = strategyMap;
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

    public Cost getCost(LimitedPartnershipDto limitedPartnershipDto) throws ServiceException {
        PostTransitionStrategy strategy = strategyMap.get(limitedPartnershipDto.getData().getKind());

        if (strategy == null) {
            throw new ServiceException("No strategy found for kind: " + limitedPartnershipDto.getData().getKind());
        }

        Cost cost = getCostObject();

        String limitedPartnershipKind = limitedPartnershipDto.getData().getKind() != null ? limitedPartnershipDto.getData().getKind() : FILING_KIND_LIMITED_PARTNERSHIP;

        cost.setResourceKind(limitedPartnershipKind);

        return strategy.getCost(cost);
    }

    private static final String PAYMENT_ACCOUNT = "data-maintenance";
    private static final String CREDIT_CARD = "credit-card";
    private static final String PAYMENT_SESSION = "payment-session#payment-session";

    private Cost getCostObject() {
        Cost cost = new Cost();

        cost.setAvailablePaymentMethods(Collections.singletonList(CREDIT_CARD));
        cost.setClassOfPayment(Collections.singletonList(PAYMENT_ACCOUNT));
        cost.setKind(PAYMENT_SESSION);

        return cost;
    }
}
