package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipPatchDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * In many of these tests, it's only necessary to test with one of the JsonNullable fields in the Patch class changing
 * as the behaviour should be the same for all of them.
 */
@SpringBootTest
class LimitedPartnershipPatchMapperTest {

    private static final String JSON_WITH_MISSING_FIELDS = "{\"email\":\"test@test.com\"}";
    private static final String JSON_WITH_VALID_FIELDS_ALL_PRESENT = "{\"partnership_name\":\"Asset Adders\", \"email\":\"test@test.com\", \"jurisdiction\":\"Scotland\"}";
    private static final String JSON_WITH_INVALID_JURISDICTION = "{\"partnership_name\":\"Asset Adders\", \"email\":\"test@test.com\", \"jurisdiction\":\"Mongolia\"}";


    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private LimitedPartnershipPatchMapper patchMapper;

    @Test
    void testObjectMapperCanHandleJsonNullableFields() throws JsonProcessingException {
        assertEquals("some description", mapper.readValue("{\"partnership_name\":\"some description\"}",
                LimitedPartnershipPatchDto.class).getPartnershipName());
        assertNull(mapper.readValue("{\"partnership_name\":null}",
                LimitedPartnershipPatchDto.class).getPartnershipName());
        assertNull(mapper.readValue("{}", LimitedPartnershipPatchDto.class).getPartnershipName());
    }

    @ParameterizedTest
    @CsvSource(value = {
            // Fields NOT present in the JSON - no update:
            JSON_WITH_MISSING_FIELDS + "$ Asset Strippers $ test@test.com $ Scotland",
            // Fields ARE present in the JSON - set the new string value:
            JSON_WITH_VALID_FIELDS_ALL_PRESENT + "$ Asset Adders $ test@test.com $ Scotland",
            // Jurisdiction field is invalid in the JSON - set the 'Unknown' value:
            JSON_WITH_INVALID_JURISDICTION + "$ Asset Adders $ test@test.com $ Unknown"
    }, delimiter = '$')
    void testMappingWhenEmailValueSentAndNameUnchanged(String incomingJson,
                                                       String expectedPartnershipName,
                                                       String expectedEmail,
                                                       String expectedJurisdiction)
            throws JsonProcessingException {
        // Given
        LimitedPartnershipPatchDto patchDto = mapper.readValue(incomingJson, LimitedPartnershipPatchDto.class);

        DataDto mongoDto = createMongoDto();

        // When
        patchMapper.update(patchDto, mongoDto);

        // Then
        checkExpectedFieldValues(mongoDto, expectedPartnershipName, expectedEmail, expectedJurisdiction);
    }

    private DataDto createMongoDto() {
        DataDto mongoDto = new DataDto();
        mongoDto.setPartnershipName("Asset Strippers");
        mongoDto.setNameEnding(PartnershipNameEnding.L_DOT_P_DOT);
        mongoDto.setPartnershipType(PartnershipType.PFLP);
        mongoDto.setJurisdiction(Jurisdiction.SCOTLAND);

        return mongoDto;
    }

    private void checkExpectedFieldValues(DataDto mongoDto,
                                          String expectedPartnershipName,
                                          String expectedEmail,
                                          String expectedJurisdiction) {
        assertEquals(expectedPartnershipName, mongoDto.getPartnershipName());
        assertEquals(PartnershipNameEnding.L_DOT_P_DOT.getDescription(), mongoDto.getNameEnding());
        assertEquals(PartnershipType.PFLP, mongoDto.getPartnershipType());
        assertEquals(expectedEmail, mongoDto.getEmail());
        assertEquals(expectedJurisdiction, mongoDto.getJurisdiction());
    }
}