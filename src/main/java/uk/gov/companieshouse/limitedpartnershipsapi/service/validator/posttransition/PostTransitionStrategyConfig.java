package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;

import java.util.Map;

@Configuration
public class PostTransitionStrategyConfig {
    @Autowired
    private UpdateRegisteredOfficeAddress updateRegisteredOfficeAddress;

    @Autowired
    private UpdatePartnershipName updatePartnershipName;

    @Bean
    public Map<String, PostTransitionStrategy> strategyMap() {
        return Map.of(
                PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS.getDescription(), updateRegisteredOfficeAddress,
                PartnershipKind.UPDATE_PARTNERSHIP_NAME.getDescription(), updatePartnershipName
        );
    }
}
