package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;

import java.time.LocalDate;

public class LimitedPartnerDataDao {

    @Field("appointment_id")
    private String appointmentId;

    @Field("contribution_currency_type")
    private Currency contributionCurrencyType;

    @Field("contribution_currency_value")
    private String contributionCurrencyValue;

    @Field("contribution_non_monetary_value")
    private String contributionNonMonetaryValue;

    @Field("date_effective_from")
    private LocalDate dateEffectiveFrom;

    @Field("date_of_birth")
    private LocalDate dateOfBirth;

    @Field("etag")
    private String etag;

    @Field("forename")
    private String forename;

    @Field("former_names")
    private String formerNames;

    @Field("surname")
    private String surname;

    @Field("governing_law")
    private String governingLaw;

    @Field("legal_personality_statement_checked")
    private boolean legalPersonalityStatementChecked;

    @Field("kind")
    private String kind;

    @Field("legal_entity_name")
    private String legalEntityName;

    @Field("legal_entity_register_name")
    private String legalEntityRegisterName;

    @Field("legal_entity_registration_location")
    private String legalEntityRegistrationLocation;

    @Field("legal_form")
    private String legalForm;

    @Field("nationality1")
    private String nationality1;

    @Field("nationality2")
    private String nationality2;

    @Field("principal_office_address")
    private AddressDao principalOfficeAddress;

    @Field("registered_company_number")
    private String registeredCompanyNumber;

    @Field("resignation_date")
    private LocalDate resignationDate;

    @Field("usual_residential_address")
    private AddressDao usualResidentialAddress;

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Currency getContributionCurrencyType() {
        return contributionCurrencyType;
    }

    public void setContributionCurrencyType(Currency contributionCurrencyType) {
        this.contributionCurrencyType = contributionCurrencyType;
    }

    public String getContributionCurrencyValue() {
        return contributionCurrencyValue;
    }

    public void setContributionCurrencyValue(String contributionCurrencyValue) {
        this.contributionCurrencyValue = contributionCurrencyValue;
    }

    public String getContributionNonMonetaryValue() {
        return contributionNonMonetaryValue;
    }

    public void setContributionNonMonetaryValue(String contributionNonMonetaryValue) {
        this.contributionNonMonetaryValue = contributionNonMonetaryValue;
    }

    public LocalDate getDateEffectiveFrom() {
        return dateEffectiveFrom;
    }

    public void setDateEffectiveFrom(LocalDate dateEffectiveFrom) {
        this.dateEffectiveFrom = dateEffectiveFrom;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public String getGoverningLaw() {
        return governingLaw;
    }

    public void setGoverningLaw(String governingLaw) {
        this.governingLaw = governingLaw;
    }

    public boolean getLegalPersonalityStatementChecked() {
        return legalPersonalityStatementChecked;
    }

    public void setLegalPersonalityStatementChecked(boolean legalPersonalityStatementChecked) {
        this.legalPersonalityStatementChecked = legalPersonalityStatementChecked;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
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

    public AddressDao getUsualResidentialAddress() {
        return usualResidentialAddress;
    }

    public void setUsualResidentialAddress(AddressDao usualResidentialAddress) {
        this.usualResidentialAddress = usualResidentialAddress;
    }
}