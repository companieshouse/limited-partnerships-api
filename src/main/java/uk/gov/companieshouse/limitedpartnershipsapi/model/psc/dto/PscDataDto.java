package uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.NatureOfControl;

import java.time.LocalDate;
import java.util.List;

public class PscDataDto {

    @JsonProperty("appointment_id")
    String appointmentId;

    @JsonProperty("country")
    Country country;

    @JsonProperty("date_effective_from")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateEffectiveFrom;

    @JsonProperty("date_of_birth")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateOfBirth;

    @JsonProperty("forename")
    String forename;

    @JsonProperty("former_names")
    String formerNames;

    @JsonProperty("governing_law")
    String governingLaw;

    @JsonProperty("kind")
    String kind;

    @JsonProperty("legal_entity_name")
    String legalEntityName;

    @JsonProperty("legal_entity_register_name")
    String legalEntityRegisterName;

    @JsonProperty("legal_entity_registration_location")
    Country legalEntityRegistrationLocation;

    @JsonProperty("legal_form")
    String legalForm;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty("legal_personality_statement_checked")
    Boolean legalPersonalityStatementChecked;

    @JsonProperty("nationality1")
    Nationality nationality1;

    @JsonProperty("nationality2")
    Nationality nationality2;

    @JsonProperty("natures_of_control")
    List<NatureOfControl> naturesOfControl;

    @JsonProperty("principal_office_address")
    AddressDto principalOfficeAddress;

    @JsonProperty("registered_company_number")
    String registeredCompanyNumber;

    @JsonProperty("resignation_date")
    LocalDate resignationDate;

    @JsonProperty("service_address")
    AddressDto serviceAddress;

    @JsonProperty("surname")
    String surname;

    @JsonProperty("usual_residential_address")
    AddressDto usualResidentialAddress;

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public LocalDate getDateEffectiveFrom() {
        return dateEffectiveFrom;
    }

    public void setDateEffectiveFrom(LocalDate dateEffectiveFrom) {
        this.dateEffectiveFrom = dateEffectiveFrom;
    }

    public String getLegalEntityName() {
        return legalEntityName;
    }

    public void setLegalEntityName(String legalEntityName) {
        this.legalEntityName = legalEntityName;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public Boolean getLegalPersonalityStatementChecked() {
        return legalPersonalityStatementChecked;
    }

    public void setLegalPersonalityStatementChecked(Boolean legalPersonalityStatementChecked) {
        this.legalPersonalityStatementChecked = legalPersonalityStatementChecked;
    }

    public String getGoverningLaw() {
        return governingLaw;
    }

    public void setGoverningLaw(String governingLaw) {
        this.governingLaw = governingLaw;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getLegalEntityRegisterName() {
        return legalEntityRegisterName;
    }

    public void setLegalEntityRegisterName(String legalEntityRegisterName) {
        this.legalEntityRegisterName = legalEntityRegisterName;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Country getLegalEntityRegistrationLocation() {
        return legalEntityRegistrationLocation;
    }

    public void setLegalEntityRegistrationLocation(Country legalEntityRegistrationLocation) {
        this.legalEntityRegistrationLocation = legalEntityRegistrationLocation;
    }

    public String getLegalForm() {
        return legalForm;
    }

    public void setLegalForm(String legalForm) {
        this.legalForm = legalForm;
    }

    public String getFormerNames() {
        return formerNames;
    }

    public void setFormerNames(String formerNames) {
        this.formerNames = formerNames;
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

    public List<NatureOfControl> getNaturesOfControl() {
        return naturesOfControl;
    }

    public void setNaturesOfControl(List<NatureOfControl> naturesOfControl) {
        this.naturesOfControl = naturesOfControl;
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

    public AddressDto getPrincipalOfficeAddress() {
        return principalOfficeAddress;
    }

    public void setPrincipalOfficeAddress(AddressDto principalOfficeAddress) {
        this.principalOfficeAddress = principalOfficeAddress;
    }

    public AddressDto getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(AddressDto serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public AddressDto getUsualResidentialAddress() {
        return usualResidentialAddress;
    }

    public void setUsualResidentialAddress(AddressDto usualResidentialAddress) {
        this.usualResidentialAddress = usualResidentialAddress;
    }
}

