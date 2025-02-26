package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.ValidEnum;

import java.time.LocalDate;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LONG_MAX_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MIN_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.REG_EXP_FOR_INVALID_CHARACTERS;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.SHORT_MAX_SIZE;

public class GeneralPartnerDataDto {

    public static final String NAME_MIN_SIZE_MESSAGE = "general partner name must be greater than {min}";
    public static final String NAME_MAX_SIZE_MESSAGE = "general partner name must be less than {max}";
    public static final String NAME_INVALID_MESSAGE = "general partner name is invalid";

    @JsonProperty("forename")
    @Size(min = MIN_SIZE, message = NAME_MIN_SIZE_MESSAGE)
    @Size(max = SHORT_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_INVALID_CHARACTERS, message = NAME_INVALID_MESSAGE)
    private String forename;

    @JsonProperty("former_names")
    @Size(max = LONG_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_INVALID_CHARACTERS, message = NAME_INVALID_MESSAGE)
    private String formerNames;

    @JsonProperty("surname")
    @Size(min = MIN_SIZE, message = NAME_MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_INVALID_CHARACTERS, message = NAME_INVALID_MESSAGE)
    private String surname;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @JsonProperty("nationality1")
    @ValidEnum(message = "First nationality must be valid")
    private Nationality nationality1;

    @JsonProperty("nationality2")
    @ValidEnum(message = "Second nationality must be valid")
    private Nationality nationality2;

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

    public Nationality getNationality1() {
        return nationality1;
    }

    public void setNationality1(Nationality nationality1) {
        this.nationality1 = nationality1;
    }

    public Nationality getNationality2() {
        return nationality2;
    }

    public void setNationality2(Nationality nationality2) {
        this.nationality2 = nationality2;
    }

    public boolean isNotDisqualifiedStatementChecked() {
        return isNotDisqualifiedStatementChecked;
    }

    public void setNotDisqualifiedStatementChecked(boolean notDisqualifiedStatementChecked) {
        isNotDisqualifiedStatementChecked = notDisqualifiedStatementChecked;
    }
}
