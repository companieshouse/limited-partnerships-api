package uk.gov.companieshouse.limitedpartnershipsapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrimmingRequestBodyAdviceTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final TrimmingRequestBodyAdvice advice = new TrimmingRequestBodyAdvice(jsonMapper);

    @Test
    void shouldTrimAllStringValuesIncludingNestedObjectsAndArrays() throws IOException {
        String json = """
                {
                    "data": {
                        "partnership_name": "   Test name ",
                        "partnership_type": "  LP ",
                        "completed": true,
                        "count": 3,
                        "nationality2": null,
                        "address": { "premises": "  22  " },
                        "list": [" a ", "b ", 1]
                    }
                }
                """;

        JsonNode data = jsonMapper.readTree(readBody(json, MediaType.APPLICATION_JSON)).get("data");

        assertEquals("Test name", data.get("partnership_name").stringValue());
        assertEquals("LP", data.get("partnership_type").stringValue());
        assertTrue(data.get("completed").booleanValue());
        assertEquals(3, data.get("count").intValue());
        assertTrue(data.get("nationality2").isNull());
        assertEquals("22", data.get("address").get("premises").stringValue());
        assertEquals("a", data.get("list").get(0).stringValue());
        assertEquals("b", data.get("list").get(1).stringValue());
        assertEquals(1, data.get("list").get(2).intValue());
    }

    @Test
    void shouldUpdateContentLengthToMatchTrimmedBody() throws IOException {
        HttpInputMessage result = advise("{\"name\": \"  x  \"}", MediaType.APPLICATION_JSON);

        byte[] body = result.getBody().readAllBytes();
        assertEquals(body.length, result.getHeaders().getContentLength());
    }

    @Test
    void shouldPassMalformedJsonThroughUnchanged() throws IOException {
        String malformed = "{\"name\": \"  x  \"";

        assertEquals(malformed, readBody(malformed, MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldIgnoreNonJsonContent() throws IOException {
        HttpInputMessage input = inputMessage("  plain text  ", MediaType.TEXT_PLAIN);

        assertSame(input, advice.beforeBodyRead(input, null, String.class, null));
    }

    private String readBody(String json, MediaType mediaType) throws IOException {
        return new String(advise(json, mediaType).getBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private HttpInputMessage advise(String json, MediaType mediaType) throws IOException {
        return advice.beforeBodyRead(inputMessage(json, mediaType), null, Object.class, null);
    }

    private static HttpInputMessage inputMessage(String body, MediaType mediaType) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentLength(bytes.length);

        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };
    }
}
