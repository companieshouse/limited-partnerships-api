package uk.gov.companieshouse.limitedpartnershipsapi.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
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

        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipBuilder().buildDto();

        Set<ConstraintViolation<LimitedPartnershipDto>> violations = validator.validate(
                limitedPartnershipDto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidatingPartnershipDtoWithoutNameEndingShouldNotReturnError() {

        LimitedPartnershipDto limitedPartnershipDto =
                new LimitedPartnershipBuilder().withNameEnding(null).buildDto();

        Set<ConstraintViolation<LimitedPartnershipDto>> violations = validator.validate(
                limitedPartnershipDto);

        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"LP123456", "NL332211", "SL000001"})
    void testValidatingPartnershipDtoWithValidCompanyNumberShouldNotReturnError(String partnershipNumber) {
        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipBuilder().buildDto();

        Set<ConstraintViolation<LimitedPartnershipDto>> violations = validator.validate(
                limitedPartnershipDto);

        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"lp123456", "LP12", "00006400", "NI332211", "LP3322119"})
    void testValidatingPartnershipDtoWithInvalidCompanyNumberReturnsError(String partnershipNumber) {
        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipBuilder().buildDto();

        limitedPartnershipDto.getData().setPartnershipNumber(partnershipNumber);

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

        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipBuilder().buildDto();

        limitedPartnershipDto.getData().setJurisdiction(Jurisdiction.UNKNOWN);
        limitedPartnershipDto.getData().setPartnershipType(PartnershipType.UNKNOWN);
        limitedPartnershipDto.getData().setNameEnding(PartnershipNameEnding.UNKNOWN);

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

        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipBuilder().buildDto();

        String longString161Characters = "kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk";

        limitedPartnershipDto.getData().setPartnershipName(longString161Characters);
        limitedPartnershipDto.getData().setEmail("wrong-format-email.com");

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
