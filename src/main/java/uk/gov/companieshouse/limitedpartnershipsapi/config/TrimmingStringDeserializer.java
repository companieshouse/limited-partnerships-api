package uk.gov.companieshouse.limitedpartnershipsapi.config;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.jdk.StringDeserializer;

/**
 * Trims leading and trailing whitespace from all String values in incoming JSON.
 */
public class TrimmingStringDeserializer extends StringDeserializer {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) {
        String value = super.deserialize(p, ctxt);
        return value == null ? null : value.trim();
    }
}
