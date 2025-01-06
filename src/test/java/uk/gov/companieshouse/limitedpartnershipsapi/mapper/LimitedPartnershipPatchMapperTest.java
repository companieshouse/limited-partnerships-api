package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipPatchDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * In many of these tests, it's only necessary to test with one of the JsonNullable fields in the Patch class changing
 * as the behaviour should be the same for all of them.
 */
@SpringBootTest
class LimitedPartnershipPatchMapperTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private LimitedPartnershipPatchMapper patchMapper;

    @Test
    void testObjectMapperCanHandleJsonNullableFields() throws JsonProcessingException {
        assertEquals(JsonNullable.of("some description"), mapper.readValue("{\"partnership_name\":\"some description\"}",
                LimitedPartnershipPatchDto.class).getPartnershipName());
        assertEquals(JsonNullable.of(null), mapper.readValue("{\"partnership_name\":null}",
                LimitedPartnershipPatchDto.class).getPartnershipName());
        assertNull(mapper.readValue("{}", LimitedPartnershipPatchDto.class).getPartnershipName());
    }

    @Test
    void testMapStructMappingWhenEmailValueSentAndNameUnchanged() throws JsonProcessingException {

        // Field NOT present in the JSON - no update:

        // Given
        String incomingJson = "{\"email\":\"test@test.com\"}";
        LimitedPartnershipPatchDto patchDto = mapper.readValue(incomingJson, LimitedPartnershipPatchDto.class);

        DataDto mongoDto = createMongoDto();

        // When
        patchMapper.update(patchDto, mongoDto);

        // Then
        checkExpectedFieldValues(mongoDto, "Asset Strippers");
    }

    @Test
    void testMapStructMappingWhenEmailValueSentAndNameChanged() throws JsonProcessingException {

        // Field IS present in the JSON - set the new string value:

        // Given
        String incomingJson = "{\"partnership_name\":\"Asset Adders\", \"email\":\"test@test.com\"}";
        LimitedPartnershipPatchDto patchDto = mapper.readValue(incomingJson, LimitedPartnershipPatchDto.class);

        DataDto mongoDto = createMongoDto();

        // When
        patchMapper.update(patchDto, mongoDto);

        // Then
        checkExpectedFieldValues(mongoDto, "Asset Adders");
    }

    @Test
    void testMapStructMappingWhenEmailValueSentAndNameSetToNull() throws JsonProcessingException {

        // Field IS present in the JSON with value null - set to null:

        // Given
        String incomingJson = "{\"partnership_name\":null, \"email\":\"test@test.com\"}";
        LimitedPartnershipPatchDto patchDto = mapper.readValue(incomingJson, LimitedPartnershipPatchDto.class);

        DataDto mongoDto = createMongoDto();

        // When
        patchMapper.update(patchDto, mongoDto);

        // Then
        checkExpectedFieldValues(mongoDto, null);
    }

    @Test
    void testMapStructMappingWhenEmailValueSentAndNameSetToUndefined() {

        // Field IS present in the JSON with value undefined - no update:

        // Given

        /* Need to create the patch DTO a bit differently, as this doesn't work::

             String incomingJson = "{\"partnership_name\":undefined, \"email\":\"test@test.com\"}";
             PatchDto patchDto = mapper.readValue(incomingJson, PatchDto.class);

           Error - "JsonParseException: Unrecognized token 'undefined'")
         */

        LimitedPartnershipPatchDto patchDto = new LimitedPartnershipPatchDto();
        patchDto.setPartnershipName(JsonNullable.undefined());
        patchDto.setEmail(JsonNullable.of("test@test.com"));

        DataDto mongoDto = createMongoDto();

        // When
        patchMapper.update(patchDto, mongoDto);

        // Then
        checkExpectedFieldValues(mongoDto, "Asset Strippers");
    }

    private DataDto createMongoDto() {
        DataDto mongoDto = new DataDto();
        mongoDto.setPartnershipName("Asset Strippers");
        mongoDto.setNameEnding(PartnershipNameEnding.LP);
        mongoDto.setPartnershipType(PartnershipType.PFLP);

        return mongoDto;
    }

    private void checkExpectedFieldValues(DataDto mongoDto, String expectedPartnershipName) {
        assertEquals(expectedPartnershipName, mongoDto.getPartnershipName());
        assertEquals(PartnershipNameEnding.LP, mongoDto.getNameEnding());
        assertEquals(PartnershipType.PFLP, mongoDto.getPartnershipType());
        assertEquals("test@test.com", mongoDto.getEmail());
    }
}