package uk.gov.companieshouse.limitedpartnershipsapi.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipSubmissionDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LONG_MAX_SIZE;

class LimitedPartnershipSubmissionDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testCreatePartnershipShouldNotReturnError() {

        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = new LimitedPartnershipSubmissionDto();
        DataDto dto = new DataDto();

        dto.setPartnershipName("Test name");
        dto.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
        dto.setPartnershipType(PartnershipType.LP);
        dto.setEmail("test@email.com");
        dto.setJurisdiction(Jurisdiction.ENGLAND_AND_WALES);

        limitedPartnershipSubmissionDto.setData(dto);

        Set<ConstraintViolation<LimitedPartnershipSubmissionDto>> violations = validator.validate(limitedPartnershipSubmissionDto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testCreatePartnershipWithInvalidEnumValuesReturnsErrors() {

        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = new LimitedPartnershipSubmissionDto();
        DataDto dto = new DataDto();

        dto.setPartnershipName("Test name");
        dto.setEmail("test@email.com");
        dto.setJurisdiction(Jurisdiction.UNKNOWN);
        dto.setPartnershipType(PartnershipType.UNKNOWN);
        dto.setNameEnding(PartnershipNameEnding.UNKNOWN);

        limitedPartnershipSubmissionDto.setData(dto);

        Set<ConstraintViolation<LimitedPartnershipSubmissionDto>> violations = validator.validate(limitedPartnershipSubmissionDto);

        assertFalse(violations.isEmpty());
        assertThat(violations).hasSize(3);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder("Jurisdiction must be valid", "Partnership type must be valid",
                        "Name ending must be valid");
    }

    @Test
    void testCreatePartnershipShouldReturnBadRequestErrorIfPartnershipNameIsMoreThan160Character() {

        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = new LimitedPartnershipSubmissionDto();
        DataDto dto = new DataDto();

        String longString161Characters = "kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk";

        dto.setPartnershipName(longString161Characters);
        dto.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
        dto.setPartnershipType(PartnershipType.LP);
        dto.setEmail("wrong-format-email.com");

        limitedPartnershipSubmissionDto.setData(dto);

        Set<ConstraintViolation<LimitedPartnershipSubmissionDto>> violations = validator.validate(limitedPartnershipSubmissionDto);

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
