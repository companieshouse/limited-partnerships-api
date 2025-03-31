package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipSubmissionDto;

import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.secure;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class LimitedPartnershipJsonTest {

    @Autowired
    private JacksonTester<LimitedPartnershipSubmissionDto> json;

    private LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto;

    @BeforeEach
    void setUp() {
        AddressDto addressDto = new AddressDto();
        addressDto.setAddressLine1("Line 1");
        addressDto.setAddressLine2("Line 2");
        addressDto.setRegion("Region");
        addressDto.setPostalCode("CF14 0ZL");
        addressDto.setCountry("Country");
        addressDto.setLocality("England");
        DataDto data = new DataDto();
        data.setPartnershipName(format("Random Company %s", secure().nextAlphabetic(5)));
        data.setNameEnding(PartnershipNameEnding.LP);
        data.setPartnershipType(PartnershipType.PFLP);
        data.setRegisteredOfficeAddress(addressDto);
        data.setJurisdiction(Jurisdiction.ENGLAND_AND_WALES);
        data.setEmail("test@test.com");
        data.setTerm(Term.UNKNOWN);
        data.setPrincipalPlaceOfBusinessAddress(addressDto);
        limitedPartnershipSubmissionDto = new LimitedPartnershipSubmissionDto();
        limitedPartnershipSubmissionDto.setData(data);
    }

    @Test
    void deserialize() throws Exception {
        LimitedPartnershipSubmissionDto readJson = this.json.readObject("expected.json");
        assertThat(readJson.getData().getPartnershipName()).isEqualTo("Random Company LP");
    }

    @Test
    void serialize() throws Exception {
        JsonContent<LimitedPartnershipSubmissionDto> writtenJson = this.json.write(limitedPartnershipSubmissionDto);
        assertThat(writtenJson)
                .withFailMessage("No value at JSON path \"$.partnership_name\" within json %s", writtenJson.getJson())
                .hasJsonPathStringValue("$.data.partnership_name", limitedPartnershipSubmissionDto.getData().getPartnershipName());
    }

}
