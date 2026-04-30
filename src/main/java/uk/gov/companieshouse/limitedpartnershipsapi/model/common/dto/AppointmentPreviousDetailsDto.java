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

    private String somethingElse1;

        public String getSomethingElse1() {
            return somethingElse1;
        }

        public void setSomethingElse1(String somethingElse1) {
            this.somethingElse1 = somethingElse1;
        }
    private String somethingElse2;

        public String getSomethingElse2() {
            return somethingElse2;
        }

        public void setSomethingElse2(String somethingElse2) {
            this.somethingElse2 = somethingElse2;
        }

    private String somethingElse3;

        public String getSomethingElse3() {
            return somethingElse3;
        }

        public void setSomethingElse3(String somethingElse3) {
            this.somethingElse3 = somethingElse3;
        }

    @Deprecated
    public int sonar_SHOULD_flag_this(String b) {
        String a = "Sonar won't like me";
        int v = 7;
        java.util.Date  date = new java.util.Date(2020, 1, 1); 
        return v * 42;
    }

    public int testMe(String b) {
        if (b != null && b.equals("test")) {
            return 55;
        }
        return 0;
    }

    public int testMeToo(String b) {
        if (b != null && b.equals("test")) {
            return 55;
        }
        return 0;
    }

    public int testMeTooToo(String b) {
        if (b != null && b.equals("test")) {
            return 55;
        }
        return 0;
    }

}
