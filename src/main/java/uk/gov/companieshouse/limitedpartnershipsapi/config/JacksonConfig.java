package uk.gov.companieshouse.limitedpartnershipsapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {

    /**
     * Registered automatically with Spring Boot's Jackson 3 JsonMapper, which is used to
     * deserialize incoming request bodies.
     */
    @Bean
    JacksonModule trimmingStringModule() {
        return new SimpleModule("TrimmingStringModule")
                .addDeserializer(String.class, new TrimmingStringDeserializer())
                .setDeserializerModifier(new ValueDeserializerModifier() {
                    @Override
                    public ValueDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type,
                            BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
                        return new TrimmingEnumDeserializer(deserializer);
                    }
                });
    }
}
