package uk.gov.companieshouse.limitedpartnershipsapi.model.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

public class GeneralPartnerDataDao {

    @Field("forename")
    private String forename;

    @Field("former_names")
    private String formerNames;

    @Field("surname")
    private String surname;

    @Field("date_of_birth")
    private LocalDate dateOfBirth;

    @Field("nationality1")
    private String nationality1;

    @Field("nationality2")
    private String nationality2;

    @Field("not_disqualified_statement_checked")
    private boolean isNotDisqualifiedStatementChecked;

    @Field("legal_personality_statement_checked")
    private boolean isLegalPersonalityStatementChecked;

    @Field("kind")
    private String kind;

    @Field("etag")
    private String etag;

    @Field("appointment_id")
    private String appointmentId;

    @Field("country")
    private String country;

    @Field("date_effective_from")
    private LocalDate dateEffectiveFrom;

    @Field("governing_law")
    private String governingLaw;

    @Field("legal_entity_name")
    private String legalEntityName;

    @Field("legal_entity_register_name")
    private String legalEntityRegisterName;

    @Field("legal_entity_registration_location")
    private String legalEntityRegistrationLocation;

    @Field("legal_form")
    private String legalForm;

    @Field("principal_office_address")
    private AddressDao principalOfficeAddress;

    @Field("registered_company_number")
    private String registeredCompanyNumber;

    @Field("resignation_date")
    private LocalDate resignationDate;

    @Field("service_address")
    private AddressDao serviceAddress;

    @Field("usual_residential_address")
    private AddressDao usualResidentialAddress;

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

    public boolean isLegalPersonalityStatementChecked() {
        return isLegalPersonalityStatementChecked;
    }

    public void setLegalPersonalityStatementChecked(boolean legalPersonalityStatementChecked) {
        isLegalPersonalityStatementChecked = legalPersonalityStatementChecked;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
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

    public String getLegalEntityName() {
        return legalEntityName;
    }

    public void setLegalEntityName(String legalEntityName) {
        this.legalEntityName = legalEntityName;
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
