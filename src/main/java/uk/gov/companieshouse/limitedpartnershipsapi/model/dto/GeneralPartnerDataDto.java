package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import uk.gov.companieshouse.limitedpartnershipsapi.model.LimitedPartnerType;

import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class GeneralPartnerDataDto {

    @JsonProperty("forename")
    @Size(min = DataDto.NAME_MIN_SIZE, message = DataDto.NAME_MIN_SIZE_MESSAGE)
    @Size(max = DataDto.NAME_MAX_SIZE, message = DataDto.NAME_MAX_SIZE_MESSAGE)
    private String forename;

    @JsonProperty("former_names")
    @Size(max = DataDto.NAME_MAX_SIZE, message = DataDto.NAME_MAX_SIZE_MESSAGE)
    private String formerNames;

    @JsonProperty("surname")
    @Size(min = DataDto.NAME_MIN_SIZE, message = DataDto.NAME_MIN_SIZE_MESSAGE)
    @Size(max = DataDto.NAME_MAX_SIZE, message = DataDto.NAME_MAX_SIZE_MESSAGE)
    private String surname;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @JsonProperty("nationality1")
    private String nationality1;

    @JsonProperty("nationality2")
    private String nationality2;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("etag")
    private String etag;

    @JsonInclude(NON_NULL)
    @JsonProperty("general_partner_type")
    private LimitedPartnerType partnerType;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public LimitedPartnerType getPartnerType() {
        return partnerType;
    }

    public void setPartnerType(LimitedPartnerType partnerType) {
        this.partnerType = partnerType;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getFormerNames() {
        return formerNames;
    }

    public void setFormerNames(String formerNames) {
        this.formerNames = formerNames;
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

    public String getNationality1() {
        return nationality1;
    }

    public void setNationality1(String nationality1) {
        this.nationality1 = nationality1;
    }

    public String getNationality2() {
        return nationality2;
    }

    public void setNationality2(String nationality2) {
        this.nationality2 = nationality2;
    }
}
