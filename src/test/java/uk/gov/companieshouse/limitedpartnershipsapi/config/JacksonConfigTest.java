package uk.gov.companieshouse.limitedpartnershipsapi.config;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.Term;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JacksonConfigTest {

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .addModule(new JacksonConfig().trimmingStringModule())
            .build();

    private enum PlainEnum { FIRST, SECOND }

    private record Wrapper(PlainEnum plain, List<PlainEnum> list) {}

    @Test
    void shouldTrimStringsAndEnumsWithJsonCreator() {
        String json = """
                {
                    "partnership_name": "   Test name ",
                    "name_ending": "   Limited Partnership  ",
                    "partnership_type": "  LP ",
                    "jurisdiction": " england-wales ",
                    "term": " BY_AGREEMENT "
                }
                """;

        DataDto dto = jsonMapper.readValue(json, DataDto.class);

        assertEquals("Test name", dto.getPartnershipName());
        assertEquals(PartnershipNameEnding.LIMITED_PARTNERSHIP.getDescription(), dto.getNameEnding());
        assertEquals(PartnershipType.LP, dto.getPartnershipType());
        assertEquals(Jurisdiction.ENGLAND_AND_WALES.getApiKey(), dto.getJurisdiction());
        assertEquals(Term.BY_AGREEMENT, dto.getTerm());
    }

    @Test
    void shouldTrimPlainEnumsIncludingInLists() {
        Wrapper wrapper = jsonMapper.readValue("{\"plain\": \" SECOND \", \"list\": [\" FIRST\", \"SECOND \"]}",
                Wrapper.class);

        assertEquals(PlainEnum.SECOND, wrapper.plain());
        assertEquals(List.of(PlainEnum.FIRST, PlainEnum.SECOND), wrapper.list());
    }

    @Test
    void shouldStillRejectInvalidPlainEnumValues() {
        assertThrows(InvalidFormatException.class,
                () -> jsonMapper.readValue("{\"plain\": \" INVALID \"}", Wrapper.class));
    }

    @Test
    void shouldStillMapInvalidJsonCreatorEnumValuesToUnknown() {
        DataDto dto = jsonMapper.readValue("{\"partnership_type\": \" INVALID \"}", DataDto.class);

        assertEquals(PartnershipType.UNKNOWN, dto.getPartnershipType());
    }
}
