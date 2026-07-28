package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipType;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LONG_MAX_SIZE;

class PartnershipDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidatingPartnershipDtoShouldNotReturnError() {

        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();

        Set<ConstraintViolation<PartnershipDto>> violations = validator.validate(
            partnershipDto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidatingPartnershipDtoWithoutNameEndingShouldNotReturnError() {

        PartnershipDto partnershipDto =
            new PartnershipBuilder().withNameEnding(null).buildDto();

        Set<ConstraintViolation<PartnershipDto>> violations = validator.validate(
            partnershipDto);

        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"LP123456", "NL332211", "SL000001"})
    void testValidatingPartnershipDtoWithValidCompanyNumberShouldNotReturnError(String partnershipNumber) {
        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();

        Set<ConstraintViolation<PartnershipDto>> violations = validator.validate(
            partnershipDto);

        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"lp123456", "LP12", "00006400", "NI332211", "LP3322119"})
    void testValidatingPartnershipDtoWithInvalidCompanyNumberReturnsError(String partnershipNumber) {
        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();

        partnershipDto.getData().setPartnershipNumber(partnershipNumber);

        Set<ConstraintViolation<PartnershipDto>> violations = validator.validate(
            partnershipDto);

        assertFalse(violations.isEmpty());
        assertThat(violations).hasSize(1);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder("Partnership number must be valid");
    }

    @Test
    void testValidatingPartnershipDtoWithInvalidEnumValuesReturnsErrors() {

        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();

        partnershipDto.getData().setJurisdiction(Jurisdiction.UNKNOWN);
        partnershipDto.getData().setPartnershipType(PartnershipType.UNKNOWN);
        partnershipDto.getData().setNameEnding(PartnershipNameEnding.UNKNOWN);

        Set<ConstraintViolation<PartnershipDto>> violations = validator.validate(
            partnershipDto);

        assertFalse(violations.isEmpty());
        assertThat(violations).hasSize(3);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder("Jurisdiction must be valid", "Partnership type must be valid",
                        "Name ending must be valid");
    }

    @Test
    void testValidatingPartnershipDtoShouldReturnBadRequestErrorIfPartnershipNameIsMoreThan160Character() {

        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();

        String longString161Characters = "kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk";

        partnershipDto.getData().setPartnershipName(longString161Characters);
        partnershipDto.getData().setEmail("wrong-format-email.com");

        Set<ConstraintViolation<PartnershipDto>> violations = validator.validate(
            partnershipDto);

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
