package uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.HasNationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.NatureOfControl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.PersonWithSignificantControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.validator.EnumValid;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LONG_MAX_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MAX_SIZE_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.REG_EXP_FOR_ALLOWED_CHARACTERS;

public class PersonWithSignificantControlDataDto implements HasNationality {

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("appointment_id")
    private String appointmentId;

    @JsonProperty("country")
    private Country country;

    @JsonProperty("date_effective_from")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateEffectiveFrom;

    @JsonProperty("resignation_date")
    private LocalDate resignationDate;

    @JsonProperty("natures_of_control")
    private List<NatureOfControl> naturesOfControl;

    @JsonProperty("service_address")
    @Valid
    private AddressDto serviceAddress;

    @JsonProperty("type")
    @EnumValid(message = "Type must be valid")
    private PersonWithSignificantControlType type;

    // PERSON

    @JsonProperty("forename")
    private String forename;

    @JsonProperty("former_names")
    private String formerNames;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("date_of_birth")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @JsonProperty("nationality1")
    private Nationality nationality1;

    @JsonProperty("nationality2")
    private Nationality nationality2;

    @JsonProperty("usual_residential_address")
    private AddressDto usualResidentialAddress;

    // LEGAL ENTITY && ORP

    @JsonProperty("legal_entity_name")
    @Size(max = LONG_MAX_SIZE, message = "Name " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Name " + INVALID_CHARACTERS_MESSAGE)
    private String legalEntityName;

    @JsonProperty("legal_entity_register_name")
    @Size(max = LONG_MAX_SIZE, message = "Legal entity register name " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Legal entity register name " + INVALID_CHARACTERS_MESSAGE)
    private String legalEntityRegisterName;

    @JsonProperty("governing_law")
    @Size(max = LONG_MAX_SIZE, message = "Governing law " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Governing law " + INVALID_CHARACTERS_MESSAGE)
    private String governingLaw;

    @JsonProperty("legal_entity_registration_location")
    @EnumValid(message = "Legal entity registration location must be valid")
    private Country legalEntityRegistrationLocation;

    @JsonProperty("legal_form")
    @Size(max = LONG_MAX_SIZE, message = "Legal form " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Legal form " + INVALID_CHARACTERS_MESSAGE)
    private String legalForm;

    @JsonProperty("registered_company_number")
    @Size(max = LONG_MAX_SIZE, message = "Registered company number " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Registered company number " + INVALID_CHARACTERS_MESSAGE)
    private String registeredCompanyNumber;

    @JsonProperty("principal_office_address")
    @Valid
    private AddressDto principalOfficeAddress;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Country getCountry() {
        return country;
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

    public LocalDate getResignationDate() {
        return resignationDate;
    }

    public void setResignationDate(LocalDate resignationDate) {
        this.resignationDate = resignationDate;
    }

    public List<NatureOfControl> getNaturesOfControl() {
        return naturesOfControl;
    }

    public void setNaturesOfControl(List<NatureOfControl> naturesOfControl) {
        this.naturesOfControl = naturesOfControl;
    }

    public AddressDto getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(AddressDto serviceAddress) {
        this.serviceAddress = serviceAddress;
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
        return nationality1 != null ? nationality1.getDescription() : null;
    }

    public void setNationality1(Nationality nationality1) {
        this.nationality1 = nationality1;
    }

    public String getNationality2() {
        return nationality2 != null ? nationality2.getDescription() : null;
    }

    public void setNationality2(Nationality nationality2) {
        this.nationality2 = nationality2;
    }

    public AddressDto getUsualResidentialAddress() {
        return usualResidentialAddress;
    }

    public void setUsualResidentialAddress(AddressDto usualResidentialAddress) {
        this.usualResidentialAddress = usualResidentialAddress;
    }

    public String getLegalEntityName() {
        return legalEntityName;
    }

    public void setLegalEntityName(String legalEntityName) {
        this.legalEntityName = legalEntityName;
    }

    public String getLegalForm() {
        return legalForm;
    }

    public void setLegalForm(String legalForm) {
        this.legalForm = legalForm;
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

    public Country getLegalEntityRegistrationLocation() {
        return legalEntityRegistrationLocation;
    }

    public void setLegalEntityRegistrationLocation(Country legalEntityRegistrationLocation) {
        this.legalEntityRegistrationLocation = legalEntityRegistrationLocation;
    }

    public String getRegisteredCompanyNumber() {
        return registeredCompanyNumber;
    }

    public void setRegisteredCompanyNumber(String registeredCompanyNumber) {
        this.registeredCompanyNumber = registeredCompanyNumber;
    }

    public AddressDto getPrincipalOfficeAddress() {
        return principalOfficeAddress;
    }

    public void setPrincipalOfficeAddress(AddressDto principalOfficeAddress) {
        this.principalOfficeAddress = principalOfficeAddress;
    }

    public PersonWithSignificantControlType getType() {
        return type;
    }

    public void setType(PersonWithSignificantControlType type) {
        this.type = type;
    }
}

