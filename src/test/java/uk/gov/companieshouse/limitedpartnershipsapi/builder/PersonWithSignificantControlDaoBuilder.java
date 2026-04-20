package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.PersonWithSignificantControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDataDao;

public class PersonWithSignificantControlDaoBuilder {
    private String id;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private Map<String, String> links;
    private String transactionId;
    private PersonWithSignificantControlDataDao data;

    public PersonWithSignificantControlDaoBuilder withId(String id) {
        this.id = id;
        return this;
    }
    public PersonWithSignificantControlDaoBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    public PersonWithSignificantControlDaoBuilder withCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }
    public PersonWithSignificantControlDaoBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    public PersonWithSignificantControlDaoBuilder withUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }
    public PersonWithSignificantControlDaoBuilder withLinks(Map<String, String> links) {
        this.links = links;
        return this;
    }
    public PersonWithSignificantControlDaoBuilder withTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }
    public PersonWithSignificantControlDaoBuilder withData(PersonWithSignificantControlDataDao data) {
        this.data = data;
        return this;
    }

    public PersonWithSignificantControlDao build() {
        PersonWithSignificantControlDao dao = new PersonWithSignificantControlDao();
        dao.setId(id);
        dao.setCreatedAt(createdAt);
        dao.setCreatedBy(createdBy);
        dao.setUpdatedAt(updatedAt);
        dao.setUpdatedBy(updatedBy);
        dao.setLinks(links);
        dao.setTransactionId(transactionId);
        dao.setData(data);
        return dao;
    }

    public static class DataBuilder {
        private final PersonWithSignificantControlDataDao data = new PersonWithSignificantControlDataDao();

        public DataBuilder withEtag(String etag) {
            data.setEtag(etag);
            return this;
        }
        public DataBuilder withKind(String kind) {
            data.setKind(kind);
            return this;
        }
        public DataBuilder withAppointmentId(String appointmentId) {
            data.setAppointmentId(appointmentId);
            return this;
        }
        public DataBuilder withCountry(String country) {
            data.setCountry(country);
            return this;
        }
        public DataBuilder withDateEffectiveFrom(LocalDate dateEffectiveFrom) {
            data.setDateEffectiveFrom(dateEffectiveFrom);
            return this;
        }
        public DataBuilder withResignationDate(LocalDate resignationDate) {
            data.setResignationDate(resignationDate);
            return this;
        }
        public DataBuilder withNaturesOfControl(List<String> naturesOfControl) {
            data.setNaturesOfControl(naturesOfControl);
            return this;
        }
        public DataBuilder withServiceAddress(AddressDao serviceAddress) {
            data.setServiceAddress(serviceAddress);
            return this;
        }
        public DataBuilder withForename(String forename) {
            data.setForename(forename);
            return this;
        }
        public DataBuilder withFormerNames(String formerNames) {
            data.setFormerNames(formerNames);
            return this;
        }
        public DataBuilder withSurname(String surname) {
            data.setSurname(surname);
            return this;
        }
        public DataBuilder withDateOfBirth(LocalDate dateOfBirth) {
            data.setDateOfBirth(dateOfBirth);
            return this;
        }
        public DataBuilder withNationality1(String nationality1) {
            data.setNationality1(nationality1);
            return this;
        }
        public DataBuilder withNationality2(String nationality2) {
            data.setNationality2(nationality2);
            return this;
        }
        public DataBuilder withUsualResidentialAddress(AddressDao usualResidentialAddress) {
            data.setUsualResidentialAddress(usualResidentialAddress);
            return this;
        }
        public DataBuilder withLegalEntityName(String legalEntityName) {
            data.setLegalEntityName(legalEntityName);
            return this;
        }
        public DataBuilder withLegalForm(String legalForm) {
            data.setLegalForm(legalForm);
            return this;
        }
        public DataBuilder withGoverningLaw(String governingLaw) {
            data.setGoverningLaw(governingLaw);
            return this;
        }
        public DataBuilder withLegalEntityRegisterName(String legalEntityRegisterName) {
            data.setLegalEntityRegisterName(legalEntityRegisterName);
            return this;
        }
        public DataBuilder withLegalEntityRegistrationLocation(String legalEntityRegistrationLocation) {
            data.setLegalEntityRegistrationLocation(legalEntityRegistrationLocation);
            return this;
        }
        public DataBuilder withRegisteredCompanyNumber(String registeredCompanyNumber) {
            data.setRegisteredCompanyNumber(registeredCompanyNumber);
            return this;
        }
        public DataBuilder withPrincipalOfficeAddress(AddressDao principalOfficeAddress) {
            data.setPrincipalOfficeAddress(principalOfficeAddress);
            return this;
        }
        public DataBuilder withType(PersonWithSignificantControlType type) {
            data.setType(type);
            return this;
        }
        public PersonWithSignificantControlDataDao build() {
            return data;
        }
    }
}
