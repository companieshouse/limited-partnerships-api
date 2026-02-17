package uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class AppointmentPreviousDetailsDto {
    @JsonProperty("forename")
    private String forename;
    @JsonProperty("surname")
    private String surname;
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;
    @JsonProperty("legal_entity_name")
    private String legalEntityName;

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getLegalEntityName() {
        return legalEntityName;
    }

    public void setLegalEntityName(String legalEntityName) {
        this.legalEntityName = legalEntityName;
    }

    private String somethingElse;

        public String getSomethingElse() {
            return somethingElse;
        }

        public void setSomethingElse(String somethingElse) {
            this.somethingElse = somethingElse;
        }

}
