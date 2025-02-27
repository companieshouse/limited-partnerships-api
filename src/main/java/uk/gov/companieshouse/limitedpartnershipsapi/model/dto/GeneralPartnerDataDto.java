package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.ValidEnum;

import java.time.LocalDate;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LONG_MAX_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MIN_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.REG_EXP_FOR_ALLOWED_CHARACTERS;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.SHORT_MAX_SIZE;

public class GeneralPartnerDataDto {

    public static final String NAME_MIN_SIZE_MESSAGE = "general partner name must be greater than {min}";
    public static final String NAME_MAX_SIZE_MESSAGE = "general partner name must be less than {max}";
    public static final String NAME_INVALID_MESSAGE = "general partner name is invalid";

    @JsonProperty("forename")
    @Size(min = MIN_SIZE, message = NAME_MIN_SIZE_MESSAGE)
    @Size(max = SHORT_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = NAME_INVALID_MESSAGE)
    private String forename;

    @JsonProperty("former_names")
    @Size(max = LONG_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = NAME_INVALID_MESSAGE)
    private String formerNames;

    @JsonProperty("surname")
    @Size(min = MIN_SIZE, message = NAME_MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = NAME_INVALID_MESSAGE)
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

    @JsonProperty("country")
    @Size(min = MIN_SIZE, message = NAME_MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = NAME_INVALID_MESSAGE)
    private String country;

    @JsonProperty("date_effective_from")
    private LocalDate dateEffectiveFrom;

    @JsonProperty("governing_law")
    @Size(min = MIN_SIZE, message = NAME_MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = NAME_INVALID_MESSAGE)
    private String governingLaw;

    @JsonProperty("legal_entity_register_name")
    @Size(min = MIN_SIZE, message = NAME_MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = NAME_INVALID_MESSAGE)
    private String legalEntityRegisterName;

    @JsonProperty("legal_entity_registration_location")
    @Size(min = MIN_SIZE, message = NAME_MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = NAME_INVALID_MESSAGE)
    private String legalEntityRegistrationLocation;

    @JsonProperty("legal_form")
    @Size(min = MIN_SIZE, message = NAME_MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = NAME_INVALID_MESSAGE)
    private String legalForm;

    @JsonProperty("principal_office_address")
    @Valid
    private AddressDao principalOfficeAddress;

    @JsonProperty("registered_company_number")
    @Size(min = MIN_SIZE, message = NAME_MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = NAME_INVALID_MESSAGE)
    private String registeredCompanyNumber;

    @JsonProperty("resignation_date")
    private LocalDate resignationDate;

    @JsonProperty("service_address")
    @Valid
    private AddressDao serviceAddress;

    @JsonProperty("usual_residential_address")
    @Valid
    private AddressDao usualResidentialAddress;

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
        return nationality1 != null ? nationality1.getDescription() : null;
    }

    public void setNationality1(Nationality nationality1) {
        this.nationality1 = nationality1;
    }

    public String getNationality2() {
        return  nationality2 != null ? nationality2.getDescription() : null;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocalDate getDateEffectiveFrom() {
        return dateEffectiveFrom;
    }

    public void setDateEffectiveFrom(LocalDate dateEffectiveFrom) {
        this.dateEffectiveFrom = dateEffectiveFrom;
    }

    public String getGoverningLaw() {
        return governingLaw;
    }

    public void setGoverningLaw(String governingLaw) {
        this.governingLaw = governingLaw;
    }

    public String getLegalEntityRegisterName() {
        return legalEntityRegisterName;
    }

    public void setLegalEntityRegisterName(String legalEntityRegisterName) {
        this.legalEntityRegisterName = legalEntityRegisterName;
    }

    public String getLegalEntityRegistrationLocation() {
        return legalEntityRegistrationLocation;
    }

    public void setLegalEntityRegistrationLocation(String legalEntityRegistrationLocation) {
        this.legalEntityRegistrationLocation = legalEntityRegistrationLocation;
    }

    public String getLegalForm() {
        return legalForm;
    }

    public void setLegalForm(String legalForm) {
        this.legalForm = legalForm;
    }

    public AddressDao getPrincipalOfficeAddress() {
        return principalOfficeAddress;
    }

    public void setPrincipalOfficeAddress(AddressDao principalOfficeAddress) {
        this.principalOfficeAddress = principalOfficeAddress;
    }

    public String getRegisteredCompanyNumber() {
        return registeredCompanyNumber;
    }

    public void setRegisteredCompanyNumber(String registeredCompanyNumber) {
        this.registeredCompanyNumber = registeredCompanyNumber;
    }

    public LocalDate getResignationDate() {
        return resignationDate;
    }

    public void setResignationDate(LocalDate resignationDate) {
        this.resignationDate = resignationDate;
    }

    public AddressDao getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(AddressDao serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public AddressDao getUsualResidentialAddress() {
        return usualResidentialAddress;
    }

    public void setUsualResidentialAddress(AddressDao usualResidentialAddress) {
        this.usualResidentialAddress = usualResidentialAddress;
    }
}
