package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNullElse;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;

@Component
public class PostTransitionStrategyHandler {

    private final Validator validator;
    private final ValidationStatus validationStatus;

    Map<String, PostTransitionStrategy<?>> strategyMap;

    private static final String PAYMENT_ACCOUNT = "data-maintenance";
    private static final String CREDIT_CARD = "credit-card";
    private static final String PAYMENT_SESSION = "payment-session#payment-session";
    private static final String DESCRIPTION_IDENTIFIER = "description-identifier";
    private static final String RESOURCE_KIND = "limited-partnership";
    private static final String KEY = "Key";
    private static final String VALUE = "Value";

    @Autowired
    public PostTransitionStrategyHandler(Validator validator, ValidationStatus validationStatus, ObjectProvider<PostTransitionStrategy<?>> strategies) {

        this.validator = validator;
        this.validationStatus = validationStatus;

        this.strategyMap = setStrategyMap(strategies);
    }

    public List<ValidationStatusError> validateLimitedPartnership(LimitedPartnershipDto limitedPartnershipDto, Transaction transaction) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        List<ValidationStatusError> errorsList = new ArrayList<>();

        validateDto(limitedPartnershipDto, errorsList);

        if (limitedPartnershipDto.getData().getRedesignateToPFLPConfirm() != Boolean.TRUE &&
                limitedPartnershipDto.getData().getDateOfUpdate() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Date of update is required",
                    "data.dateOfUpdate"));
        }

        String kind = requireNonNullElse(limitedPartnershipDto.getData().getKind(), FILING_KIND_LIMITED_PARTNERSHIP);

        PostTransitionStrategy<LimitedPartnershipDto> strategy = (PostTransitionStrategy<LimitedPartnershipDto>) getStrategy(kind);

        strategy.validate(limitedPartnershipDto, errorsList, validationStatus, transaction);

        return errorsList;
    }

    public List<ValidationStatusError> validatePartner(PartnerDto partnerDto, Transaction transaction) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        List<ValidationStatusError> errorsList = new ArrayList<>();

        validateDto(partnerDto, errorsList);

        String kind = partnerDto.getData().getKind();

        PostTransitionStrategy<PartnerDto> strategy = (PostTransitionStrategy<PartnerDto>) getStrategy(kind);

        strategy.validate(partnerDto, errorsList, validationStatus, transaction);

        return errorsList;
    }

    private <T> void validateDto(T dto, List<ValidationStatusError> errorsList) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);

        if (!violations.isEmpty()) {
            violations.forEach(violation ->
                    errorsList.add(validationStatus.createValidationStatusError(violation.getMessage(), violation.getPropertyPath().toString()))
            );
        }
    }

    public <T> Cost getCost(T dto) throws ServiceException {
        String kind = null;

        if (dto instanceof LimitedPartnershipDto limitedPartnershipDto) {
            kind = requireNonNullElse(limitedPartnershipDto.getData().getKind(), FILING_KIND_LIMITED_PARTNERSHIP);
        } else if (dto instanceof PartnerDto partnerDto) {
            kind = partnerDto.getData().getKind();
        }

        PostTransitionStrategy<?> strategy = getStrategy(kind);

        Cost cost = getCostObject();

        cost.setResourceKind(kind);

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

    private Map<String, PostTransitionStrategy<?>> setStrategyMap(ObjectProvider<PostTransitionStrategy<?>> strategies) {

        return strategies.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        PostTransitionStrategy::getKind,
                        strategy -> strategy
                ));
    }

    private static void throwErrorIfNoStrategyFound(String kind, PostTransitionStrategy<?> strategy) throws ServiceException {
        if (strategy == null) {
            throw new ServiceException("No strategy found for kind: " + kind);
        }
    }

    private PostTransitionStrategy<?> getStrategy(String kind) throws ServiceException {
        PostTransitionStrategy<?> strategy = strategyMap.get(kind);

        throwErrorIfNoStrategyFound(kind, strategy);

        return strategy;
    }
}
