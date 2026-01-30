package uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompanyPreviousDetailsDto {
    @JsonProperty("company_name")
    private String companyName;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
