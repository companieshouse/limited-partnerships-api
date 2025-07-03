package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.Collections;

@Service
public class CostsService {

    private final LimitedPartnershipIncorporationRepository repository;

    public CostsService(LimitedPartnershipIncorporationRepository repository) {
        this.repository = repository;
    }

    @Value("${LP_REGISTRATION_COST}")
    private String registrationCostAmount;

    private static final String REGISTER_COST_DESCRIPTION = "Register Limited Partnership fee";

    private static final String PAYMENT_ACCOUNT = "data-maintenance";
    private static final String RESOURCE_KIND = "limited-partnership";
    private static final String REGISTER_PRODUCT_TYPE = "register-limited-partnership"; // used by payment-reconciliation-consumer
    private static final String CREDIT_CARD = "credit-card";
    private static final String DESCRIPTION_IDENTIFIER = "description-identifier";
    private static final String PAYMENT_SESSION = "payment-session#payment-session";
    private static final String KEY = "Key";
    private static final String VALUE = "Value";

    public Cost getCost(String incorporationId, String requestId) throws ResourceNotFoundException {
        LimitedPartnershipIncorporationDao incorporationDao = repository.findById(incorporationId).orElseThrow(() -> new ResourceNotFoundException(String.format("Incorporation with id %s not found", incorporationId)));

        ApiLogger.infoContext(requestId, String.format("Cost for incorporation with id: %s and kind: %s", incorporationId, incorporationDao.getData().getKind()));

        return getCostForRegistration();
    }

    public Cost getCostForRegistration() {
        Cost cost = new Cost();

        cost.setAmount(registrationCostAmount);
        cost.setAvailablePaymentMethods(Collections.singletonList(CREDIT_CARD));
        cost.setClassOfPayment(Collections.singletonList(PAYMENT_ACCOUNT));
        cost.setDescription(REGISTER_COST_DESCRIPTION);
        cost.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        cost.setDescriptionValues(Collections.singletonMap(KEY, VALUE));
        cost.setKind(PAYMENT_SESSION);
        cost.setResourceKind(RESOURCE_KIND);
        cost.setProductType(REGISTER_PRODUCT_TYPE);

        return cost;
    }
}
