package uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LimitedPartnershipSubmissionCreatedResponseDto(
        @JsonProperty("id") String id) {
}
