package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.ValidEnum;

import java.time.LocalDate;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LONG_MAX_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MAX_SIZE_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MIN_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MIN_SIZE_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.REG_EXP_FOR_ALLOWED_CHARACTERS;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.SHORT_MAX_SIZE;

public class GeneralPartnerDataDto {

    public static final String FORENAME_FIELD = "forename";
    public static final String SURNAME_FIELD = "surname";
    public static final String DATE_OF_BIRTH_FIELD = "date_of_birth";
    public static final String NATIONALITY1_FIELD = "nationality1";
    public static final String NATIONALITY2_FIELD = "nationality2";
    public static final String LEGAL_ENTITY_REGISTER_NAME_FIELD = "legal_entity_register_name";
    public static final String LEGAL_ENTITY_REGISTRATION_LOCATION_FIELD = "legal_entity_registration_location";
    public static final String LEGAL_FORM_FIELD = "legal_form";
    public static final String REGISTERED_COMPANY_NUMBER_FIELD = "registered_company_number";
    public static final String COUNTRY_FIELD = "country";
    public static final String GOVERNING_LAW_FIELD = "governing_law";

    @JsonProperty(FORENAME_FIELD)
    @Size(min = MIN_SIZE, message = "Forename " + MIN_SIZE_MESSAGE)
    @Size(max = SHORT_MAX_SIZE, message = "Forename " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Forename " + INVALID_CHARACTERS_MESSAGE)
    private String forename;

    @JsonProperty("former_names")
    @Size(min = MIN_SIZE, message = "Former names " + MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = "Former names " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Former names " + INVALID_CHARACTERS_MESSAGE)
    private String formerNames;

    @JsonProperty(SURNAME_FIELD)
    @Size(min = MIN_SIZE, message = "Surname " + MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = "Surname " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Surname " + INVALID_CHARACTERS_MESSAGE)
    private String surname;

    @JsonProperty(DATE_OF_BIRTH_FIELD)
    private LocalDate dateOfBirth;

    @JsonProperty(NATIONALITY1_FIELD)
    @ValidEnum(message = "First nationality must be valid")
    private Nationality nationality1;

    @JsonProperty(NATIONALITY2_FIELD)
    @ValidEnum(message = "Second nationality must be valid")
    private Nationality nationality2;

    @JsonProperty("not_disqualified_statement_checked")
    private boolean isNotDisqualifiedStatementChecked;

    @JsonProperty(COUNTRY_FIELD)
    @ValidEnum(message = "Country must be valid")
    private Country country;

    @JsonProperty("date_effective_from")
    private LocalDate dateEffectiveFrom;

    @JsonProperty(GOVERNING_LAW_FIELD)
    @Size(min = MIN_SIZE, message = "Governing law " + MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = "Governing law " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Governing law " + INVALID_CHARACTERS_MESSAGE)
    private String governingLaw;

    @JsonProperty(LEGAL_ENTITY_REGISTER_NAME_FIELD)
    @Size(min = MIN_SIZE, message = "Legal entity register name " + MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = "Legal entity register name " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Legal entity register name " + INVALID_CHARACTERS_MESSAGE)
    private String legalEntityRegisterName;

    @JsonProperty(LEGAL_ENTITY_REGISTRATION_LOCATION_FIELD)
    @Size(min = MIN_SIZE, message = "Legal entity registration location " + MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = "Legal entity registration location " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Legal entity registration law " + INVALID_CHARACTERS_MESSAGE)
    private String legalEntityRegistrationLocation;

    @JsonProperty(LEGAL_FORM_FIELD)
    @Size(min = MIN_SIZE, message = "Legal form " + MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = "Legal form " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Legal form " + INVALID_CHARACTERS_MESSAGE)
    private String legalForm;

    @JsonProperty("principal_office_address")
    @Valid
    private AddressDao principalOfficeAddress;

    @JsonProperty(REGISTERED_COMPANY_NUMBER_FIELD)
    @Size(min = MIN_SIZE, message = "Registered company number " + MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = "Registered company number " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Registered company number " + INVALID_CHARACTERS_MESSAGE)
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
        return country != null ? country.getDescription() : null;
    }

    public void setCountry(Country country) {
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
