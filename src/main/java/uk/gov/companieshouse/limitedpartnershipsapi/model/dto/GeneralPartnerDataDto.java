package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

import static uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.StringValidators.REG_EXP_FOR_INVALID_CHARACTERS;

public class GeneralPartnerDataDto {

    @JsonProperty("forename")
    @Size(min = DataDto.NAME_MIN_SIZE, message = DataDto.NAME_MIN_SIZE_MESSAGE)
    @Size(max = DataDto.NAME_MAX_SIZE, message = DataDto.NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_INVALID_CHARACTERS)
    private String forename;

    @JsonProperty("former_names")
    @Size(max = DataDto.NAME_MAX_SIZE, message = DataDto.NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_INVALID_CHARACTERS)
    private String formerNames;

    @JsonProperty("surname")
    @Size(min = DataDto.NAME_MIN_SIZE, message = DataDto.NAME_MIN_SIZE_MESSAGE)
    @Size(max = DataDto.NAME_MAX_SIZE, message = DataDto.NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_INVALID_CHARACTERS)
    private String surname;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @JsonProperty("nationality1")
    private String nationality1;

    @JsonProperty("nationality2")
    private String nationality2;

    @JsonProperty("not_disqualified_statement_checked")
    private boolean isNotDisqualifiedStatementChecked;

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

    public boolean isNotDisqualifiedStatementChecked() {
        return isNotDisqualifiedStatementChecked;
    }

    public void setNotDisqualifiedStatementChecked(boolean notDisqualifiedStatementChecked) {
        isNotDisqualifiedStatementChecked = notDisqualifiedStatementChecked;
    }
}
