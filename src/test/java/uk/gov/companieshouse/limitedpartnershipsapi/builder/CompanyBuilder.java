package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;

import java.time.LocalDate;

public class CompanyBuilder {
    private String subtype = "";
    private String companyStatus = "active";
    private LocalDate dateOfCreation = LocalDate.of(2023, 1, 1);
    private String type = "limited-partnership";
    private String companyName = "TEST LP";
    private String companyNumber = "LP123456";
    private String jurisdiction = "england-wales";

    public CompanyBuilder withSubtype(String subtype) {
        this.subtype = subtype;
        return this;
    }

    public CompanyBuilder withCompanyStatus(String companyStatus) {
        this.companyStatus = companyStatus;
        return this;
    }

    public CompanyBuilder withDateOfCreation(LocalDate dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
        return this;
    }

    public CompanyBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public CompanyBuilder withCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public CompanyBuilder withCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public CompanyBuilder withJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
        return this;
    }

    public CompanyProfileApi build() {
        CompanyProfileApi companyData = new CompanyProfileApi();

        companyData.setSubtype(subtype);
        companyData.setCompanyStatus(companyStatus);
        companyData.setDateOfCreation(dateOfCreation);
        companyData.setType(type);
        companyData.setCompanyName(companyName);
        companyData.setCompanyNumber(companyNumber);
        companyData.setJurisdiction(jurisdiction);

        return companyData;
    }

}
