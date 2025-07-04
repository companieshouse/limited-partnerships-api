package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDtoBuilder.limitedPartnerDataDtoBuilder;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@SpringBootTest
class LimitedPartnerDataDtoValidationTest {

    @Autowired
    private Validator validator;

    @Test
    public void shouldPassValidation() {
        // Given
        LimitedPartnerDataDto limitedPartnerDataDto = limitedPartnerDataDtoBuilder()
                .withContributionCurrencyValue(null)
                .build();

        // When
        Errors errors = validator.validateObject(limitedPartnerDataDto);

        // Then
        assertThat(errors.getAllErrors()).isEmpty();
    }

    @Test
    void shouldFailGivenCurrencyValueIsMinus() {
        // Given
        LimitedPartnerDataDto limitedPartnerDataDto = limitedPartnerDataDtoBuilder()
                .withContributionCurrencyValue(new BigDecimal("-0.01"))
                .build();

        // When
        Errors errors = validator.validateObject(limitedPartnerDataDto);

        // Then
        assertThat(errors.getAllErrors())
                .satisfiesExactly(
                        error -> assertThat(error.getDefaultMessage())
                                .isEqualTo("Contribution currency value must be greater than or equal to 0.00"));
    }
}
