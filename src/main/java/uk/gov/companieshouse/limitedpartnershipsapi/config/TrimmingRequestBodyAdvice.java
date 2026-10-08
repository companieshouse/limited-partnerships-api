package uk.gov.companieshouse.limitedpartnershipsapi.config;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Trims leading and trailing whitespace from every string value in incoming JSON request bodies
 * before they are bound to request DTOs.
 */
@ControllerAdvice
public class TrimmingRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private final JsonMapper jsonMapper;

    public TrimmingRequestBodyAdvice(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        MediaType contentType = inputMessage.getHeaders().getContentType();
        if (contentType == null || !MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            return inputMessage;
        }

        byte[] body = inputMessage.getBody().readAllBytes();
        byte[] trimmedBody;
        try {
            trimmedBody = body.length == 0 ? body : jsonMapper.writeValueAsBytes(trim(jsonMapper.readTree(body)));
        } catch (JacksonException e) {
            // Leave malformed JSON for the message converter to reject with its usual error
            trimmedBody = body;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.putAll(inputMessage.getHeaders());
        headers.setContentLength(trimmedBody.length);
        byte[] finalBody = trimmedBody;

        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(finalBody);
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };
    }

    private JsonNode trim(JsonNode node) {
        if (node.isString()) {
            return StringNode.valueOf(node.stringValue().trim());
        }
        if (node instanceof ObjectNode objectNode) {
            for (String name : new ArrayList<>(objectNode.propertyNames())) {
                objectNode.set(name, trim(objectNode.get(name)));
            }
        } else if (node instanceof ArrayNode arrayNode) {
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, trim(arrayNode.get(i)));
            }
        }
        return node;
    }
}
