package uk.gov.companieshouse.limitedpartnershipsapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.PatchMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.PatchDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class JsonNullableTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PatchMapper patchMapper;

    @Test
    void testObjectMapperCanHandleJsonNullableFields() throws JsonProcessingException {
        assertEquals(JsonNullable.of("some description"), mapper.readValue("{\"partnership_name\":\"some description\"}", PatchDto.class).getPartnershipName());
        assertEquals(JsonNullable.of(null), mapper.readValue("{\"partnership_name\":null}", PatchDto.class).getPartnershipName());
        assertNull(mapper.readValue("{}", PatchDto.class).getPartnershipName());
    }

    @Test
    void testMapStructMappingWhenEmailValueSentAndNameUnchanged() throws JsonProcessingException {

        // Field NOT present in the JSON - no update:

        // Given
        String incomingJson = "{\"email\":\"test@test.com\"}";
        PatchDto patchDto = mapper.readValue(incomingJson, PatchDto.class);

        DataDto mongoDto = createMongoDto();

        // When
        patchMapper.update(patchDto, mongoDto);

        // Then
        assertEquals("Asset Strippers", mongoDto.getPartnershipName());
        assertEquals(PartnershipNameEnding.LP, mongoDto.getNameEnding());
        assertEquals("test@test.com", mongoDto.getEmail());
    }

    @Test
    void testMapStructMappingWhenEmailValueSentAndNameChanged() throws JsonProcessingException {

        // Field IS present in the JSON - set the new string value:

        // Given
        String incomingJson = "{\"partnership_name\":\"Asset Adders\", \"email\":\"test@test.com\"}";
        PatchDto patchDto = mapper.readValue(incomingJson, PatchDto.class);

        DataDto mongoDto = createMongoDto();

        // When
        patchMapper.update(patchDto, mongoDto);

        // Then
        assertEquals("Asset Adders", mongoDto.getPartnershipName());
        assertEquals(PartnershipNameEnding.LP, mongoDto.getNameEnding());
        assertEquals("test@test.com", mongoDto.getEmail());
    }

    @Test
    void testMapStructMappingWhenEmailValueSentAndNameSetToNull() throws JsonProcessingException {

        // Field IS present in the JSON with value null - set to null:

        // Given
        String incomingJson = "{\"partnership_name\":null, \"email\":\"test@test.com\"}";
        PatchDto patchDto = mapper.readValue(incomingJson, PatchDto.class);

        DataDto mongoDto = createMongoDto();

        // When
        patchMapper.update(patchDto, mongoDto);

        // Then
        assertEquals(null, mongoDto.getPartnershipName());
        assertEquals(PartnershipNameEnding.LP, mongoDto.getNameEnding());
        assertEquals("test@test.com", mongoDto.getEmail());
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

        PatchDto patchDto = new PatchDto();
        patchDto.setPartnershipName(JsonNullable.undefined());
        patchDto.setEmail("test@test.com");

        DataDto mongoDto = createMongoDto();

        // When
        patchMapper.update(patchDto, mongoDto);

        // Then
        assertEquals("Asset Strippers", mongoDto.getPartnershipName());
        assertEquals(PartnershipNameEnding.LP, mongoDto.getNameEnding());
        assertEquals("test@test.com", mongoDto.getEmail());
    }

    private DataDto createMongoDto() {
        DataDto mongoDto = new DataDto();
        mongoDto.setPartnershipName("Asset Strippers");
        mongoDto.setNameEnding(PartnershipNameEnding.LP);

        return mongoDto;
    }
}
