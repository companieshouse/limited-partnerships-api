package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LimitedPartnerSubmissionCreatedResponseDto(@JsonProperty("id") String id) {
}
