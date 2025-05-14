package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.payment.Cost;

import java.util.Collections;

@Service
public class CostsService {
    @Value("${LP_REGISTRATION_COST}")
    private String registrationCostAmount;

    private static final String REGISTER_COST_DESCRIPTION = "Register Limited Partnership fee";

    private static final String PAYMENT_ACCOUNT = "data-maintenance";
    private static final String RESOURCE_KIND = "limited-partnership";
    private static final String REGISTER_PRODUCT_TYPE = "register-limited-partnership-entity";
    private static final String CREDIT_CARD = "credit-card";
    private static final String DESCRIPTION_IDENTIFIER = "description-identifier";
    private static final String PAYMENT_SESSION = "payment-session#payment-session";
    private static final String KEY = "Key";
    private static final String VALUE = "Value";



    public Cost getCost() {
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
