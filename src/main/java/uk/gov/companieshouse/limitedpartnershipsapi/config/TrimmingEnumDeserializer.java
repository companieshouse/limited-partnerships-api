package uk.gov.companieshouse.limitedpartnershipsapi.config;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.DelegatingDeserializer;
import tools.jackson.databind.util.TokenBuffer;

public class TrimmingEnumDeserializer extends DelegatingDeserializer {

    public TrimmingEnumDeserializer(ValueDeserializer<?> delegatee) {
        super(delegatee);
    }

    @Override
    protected ValueDeserializer<?> newDelegatingInstance(ValueDeserializer<?> newDelegatee) {
        return new TrimmingEnumDeserializer(newDelegatee);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) {
        if (p.hasToken(JsonToken.VALUE_STRING)) {
            String text = p.getString();
            String trimmed = text.trim();
            if (!trimmed.equals(text)) {
                // Enum deserializers read the raw token text, so replay a trimmed copy of the token
                try (TokenBuffer buffer = ctxt.bufferForInputBuffering(p)) {
                    buffer.writeString(trimmed);
                    try (JsonParser trimmedParser = buffer.asParserOnFirstToken(ctxt)) {
                        return _delegatee.deserialize(trimmedParser, ctxt);
                    }
                }
            }
        }
        return _delegatee.deserialize(p, ctxt);
    }
}
