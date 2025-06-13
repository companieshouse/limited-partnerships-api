package uk.gov.companieshouse.limitedpartnershipsapi.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LONG_MAX_SIZE;

class LimitedPartnershipDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidatingPartnershipDtoShouldNotReturnError() {

        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipDto();
        DataDto dto = new DataDto();

        dto.setPartnershipName("Test name");
        dto.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
        dto.setPartnershipType(PartnershipType.LP);
        dto.setEmail("test@email.com");
        dto.setJurisdiction(Jurisdiction.ENGLAND_AND_WALES);

        limitedPartnershipDto.setData(dto);

        Set<ConstraintViolation<LimitedPartnershipDto>> violations = validator.validate(
                limitedPartnershipDto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidatingPartnershipDtoWithoutNameEndingShouldNotReturnError() {

        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipDto();
        DataDto dto = new DataDto();

        dto.setPartnershipName("Test name");
        dto.setPartnershipType(PartnershipType.LP);

        limitedPartnershipDto.setData(dto);

        Set<ConstraintViolation<LimitedPartnershipDto>> violations = validator.validate(
                limitedPartnershipDto);

        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "LP123456", "NL332211", "SL000001" })
    void testValidatingPartnershipDtoWithValidCompanyNumberShouldNotReturnError(String partnershipNumber) {
        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipDto();
        DataDto dto = new DataDto();

        dto.setPartnershipName("Test name");
        dto.setPartnershipType(PartnershipType.LP);
        dto.setPartnershipNumber(partnershipNumber);

        limitedPartnershipDto.setData(dto);

        Set<ConstraintViolation<LimitedPartnershipDto>> violations = validator.validate(
                limitedPartnershipDto);

        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "lp123456", "LP12", "00006400", "NI332211", "LP3322119" })
    void testValidatingPartnershipDtoWithInvalidCompanyNumberReturnsError(String partnershipNumber) {
        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipDto();
        DataDto dto = new DataDto();

        dto.setPartnershipName("Test name");
        dto.setPartnershipType(PartnershipType.LP);
        dto.setPartnershipNumber(partnershipNumber);

        limitedPartnershipDto.setData(dto);

        Set<ConstraintViolation<LimitedPartnershipDto>> violations = validator.validate(
                limitedPartnershipDto);

        assertFalse(violations.isEmpty());
        assertThat(violations).hasSize(1);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder("Partnership number must be valid");
    }

    @Test
    void testValidatingPartnershipDtoWithInvalidEnumValuesReturnsErrors() {

        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipDto();
        DataDto dto = new DataDto();

        dto.setPartnershipName("Test name");
        dto.setEmail("test@email.com");
        dto.setJurisdiction(Jurisdiction.UNKNOWN);
        dto.setPartnershipType(PartnershipType.UNKNOWN);
        dto.setNameEnding(PartnershipNameEnding.UNKNOWN);

        limitedPartnershipDto.setData(dto);

        Set<ConstraintViolation<LimitedPartnershipDto>> violations = validator.validate(
                limitedPartnershipDto);

        assertFalse(violations.isEmpty());
        assertThat(violations).hasSize(3);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder("Jurisdiction must be valid", "Partnership type must be valid",
                        "Name ending must be valid");
    }

    @Test
    void testValidatingPartnershipDtoShouldReturnBadRequestErrorIfPartnershipNameIsMoreThan160Character() {

        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipDto();
        DataDto dto = new DataDto();

        String longString161Characters = "kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk";

        dto.setPartnershipName(longString161Characters);
        dto.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
        dto.setPartnershipType(PartnershipType.LP);
        dto.setEmail("wrong-format-email.com");

        limitedPartnershipDto.setData(dto);

        Set<ConstraintViolation<LimitedPartnershipDto>> violations = validator.validate(
                limitedPartnershipDto);

        assertFalse(violations.isEmpty());
        assertThat(violations).hasSize(3);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        String.format("Max length 'partnership name + name ending' is %s characters", LONG_MAX_SIZE),
                        String.format("Limited partnership name must be less than %s", LONG_MAX_SIZE),
                        "must be a well-formed email address");
    }
}
