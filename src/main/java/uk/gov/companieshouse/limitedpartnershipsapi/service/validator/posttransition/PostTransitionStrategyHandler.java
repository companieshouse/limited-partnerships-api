package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.ObjectProvider;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.firstNonNull;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;

@Component
public class PostTransitionStrategyHandler {

    private final Validator validator;
    private final ValidationStatus validationStatus;

    Map<String, PostTransitionStrategy> strategyMap;

    private static final String PAYMENT_ACCOUNT = "data-maintenance";
    private static final String CREDIT_CARD = "credit-card";
    private static final String PAYMENT_SESSION = "payment-session#payment-session";
    private static final String DESCRIPTION_IDENTIFIER = "description-identifier";
    private static final String RESOURCE_KIND = "limited-partnership";
    private static final String KEY = "Key";
    private static final String VALUE = "Value";

    @Autowired
    public PostTransitionStrategyHandler(Validator validator, ValidationStatus validationStatus, ObjectProvider<PostTransitionStrategy> strategies) {

        this.validator = validator;
        this.validationStatus = validationStatus;

        this.strategyMap = setStrategyMap(strategies);
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

        String limitedPartnershipKind = firstNonNull(limitedPartnershipDto.getData().getKind(), FILING_KIND_LIMITED_PARTNERSHIP);

        PostTransitionStrategy strategy = strategyMap.get(limitedPartnershipKind);

        throwErrorIfNoStrategyFound(limitedPartnershipDto, strategy);

        strategy.validate(limitedPartnershipDto, errorsList, validationStatus);

        return errorsList;
    }

    public Cost getCost(LimitedPartnershipDto limitedPartnershipDto) throws ServiceException {
        String limitedPartnershipKind = limitedPartnershipDto.getData().getKind() != null ? limitedPartnershipDto.getData().getKind() : FILING_KIND_LIMITED_PARTNERSHIP;

        PostTransitionStrategy strategy = strategyMap.get(limitedPartnershipKind);

        throwErrorIfNoStrategyFound(limitedPartnershipDto, strategy);

        Cost cost = getCostObject();

        cost.setResourceKind(limitedPartnershipKind);

        return strategy.getCost(cost);
    }

    private Cost getCostObject() {
        Cost cost = new Cost();

        cost.setAvailablePaymentMethods(Collections.singletonList(CREDIT_CARD));
        cost.setClassOfPayment(Collections.singletonList(PAYMENT_ACCOUNT));
        cost.setDescriptionValues(Collections.singletonMap(KEY, VALUE));
        cost.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        cost.setResourceKind(RESOURCE_KIND);
        cost.setKind(PAYMENT_SESSION);

        return cost;
    }

    private Map<String, PostTransitionStrategy> setStrategyMap(ObjectProvider<PostTransitionStrategy> strategies) {

        return strategies.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        PostTransitionStrategy::getKind,
                        strategy -> strategy
                ));
    }

    private static void throwErrorIfNoStrategyFound(LimitedPartnershipDto limitedPartnershipDto, PostTransitionStrategy strategy) throws ServiceException {
        if (strategy == null) {
            throw new ServiceException("No strategy found for kind: " + limitedPartnershipDto.getData().getKind());
        }
    }
}
