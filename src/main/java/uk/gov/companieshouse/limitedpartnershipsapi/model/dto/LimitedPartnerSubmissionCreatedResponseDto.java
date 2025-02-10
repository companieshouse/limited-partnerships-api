package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LimitedPartnerSubmissionCreatedResponseDto(String id) {
    @JsonProperty("id")
    public String id() {
        return id;
    }
}