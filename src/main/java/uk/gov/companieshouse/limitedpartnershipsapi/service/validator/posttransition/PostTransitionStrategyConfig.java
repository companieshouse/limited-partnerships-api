package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;

import java.util.Map;

@Configuration
public class PostTransitionStrategyConfig {

    @Bean
    public Map<String, PostTransitionStrategy> strategyMap() {
        return Map.of(
                PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS.getDescription(), new UpdateRegisteredOfficeAddress(),
                PartnershipKind.UPDATE_PARTNERSHIP_NAME.getDescription(), new UpdatePartnershipName()
        );
    }
}

