package uk.gov.companieshouse.limitedpartnershipsapi.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.utils.Constants.LONG_MAX_SIZE;

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
    void testCreatePartnershipWithInvalidJurisdictionReturnsErrors() {

        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = new LimitedPartnershipSubmissionDto();
        DataDto dto = new DataDto();

        dto.setPartnershipName("Test name");
        dto.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
        dto.setPartnershipType(PartnershipType.LP);
        dto.setEmail("test@email.com");
        dto.setJurisdiction(Jurisdiction.UNKNOWN);

        limitedPartnershipSubmissionDto.setData(dto);

        Set<ConstraintViolation<LimitedPartnershipSubmissionDto>> violations = validator.validate(limitedPartnershipSubmissionDto);

        assertFalse(violations.isEmpty());
        assertThat(violations).hasSize(1);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder("Jurisdiction must be valid");
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
                        String.format("partnership name must be less than %s", LONG_MAX_SIZE),
                        "must be a well-formed email address");
    }
}
