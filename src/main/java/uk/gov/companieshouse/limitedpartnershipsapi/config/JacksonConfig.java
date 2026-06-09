package uk.gov.companieshouse.limitedpartnershipsapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS)
                .registerModule(new JsonNullableModule());
        return mapper;
    }
}
