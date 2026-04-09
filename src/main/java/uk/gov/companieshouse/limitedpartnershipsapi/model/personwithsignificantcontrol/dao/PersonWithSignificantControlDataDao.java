package uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.PersonWithSignificantControlType;

import java.time.LocalDate;
import java.util.List;

public class PersonWithSignificantControlDataDao {

    @Field("etag")
    private String etag;

    @Field("kind")
    private String kind;

    @Field("appointment_id")
    private String appointmentId;

    @Field("country")
    private String country;

    @Field("date_effective_from")
    private LocalDate dateEffectiveFrom;

    @Field("resignation_date")
    private LocalDate resignationDate;

    @Field("natures_of_control")
    private List<String> naturesOfControl;

    @Field("service_address")
    private AddressDao serviceAddress;

    @Field("type")
    private PersonWithSignificantControlType type;

    // PERSON

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

    @Field("usual_residential_address")
    private AddressDao usualResidentialAddress;

    // LEGAL ENTITY

    @Field("legal_entity_name")
    private String legalEntityName;

    @Field("legal_form")
    private String legalForm;

    @Field("governing_law")
    private String governingLaw;

    @Field("legal_entity_register_name")
    private String legalEntityRegisterName;

    @Field("legal_entity_registration_location")
    private String legalEntityRegistrationLocation;

    @Field("registered_company_number")
    private String registeredCompanyNumber;

    @Field("principal_office_address")
    private AddressDao principalOfficeAddress;

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

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

    public LocalDate getResignationDate() {
        return resignationDate;
    }

    public void setResignationDate(LocalDate resignationDate) {
        this.resignationDate = resignationDate;
    }

    public List<String> getNaturesOfControl() {
        return naturesOfControl;
    }

    public void setNaturesOfControl(List<String> naturesOfControl) {
        this.naturesOfControl = naturesOfControl;
    }

    public AddressDao getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(AddressDao serviceAddress) {
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

    public AddressDao getUsualResidentialAddress() {
        return usualResidentialAddress;
    }

    public void setUsualResidentialAddress(AddressDao usualResidentialAddress) {
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

    public String getLegalEntityRegistrationLocation() {
        return legalEntityRegistrationLocation;
    }

    public void setLegalEntityRegistrationLocation(String legalEntityRegistrationLocation) {
        this.legalEntityRegistrationLocation = legalEntityRegistrationLocation;
    }

    public String getRegisteredCompanyNumber() {
        return registeredCompanyNumber;
    }

    public void setRegisteredCompanyNumber(String registeredCompanyNumber) {
        this.registeredCompanyNumber = registeredCompanyNumber;
    }

    public AddressDao getPrincipalOfficeAddress() {
        return principalOfficeAddress;
    }

    public void setPrincipalOfficeAddress(AddressDao principalOfficeAddress) {
        this.principalOfficeAddress = principalOfficeAddress;
    }

    public PersonWithSignificantControlType getType() {
        return type;
    }

    public void setType(PersonWithSignificantControlType type) {
        this.type = type;
    }
}
