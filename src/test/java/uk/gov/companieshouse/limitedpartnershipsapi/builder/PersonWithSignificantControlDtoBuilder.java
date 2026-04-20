package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import java.time.LocalDate;
import java.util.List;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.NatureOfControl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.PersonWithSignificantControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;

public class PersonWithSignificantControlDtoBuilder {
    private String id;
    private PersonWithSignificantControlDataDto data;

    public PersonWithSignificantControlDtoBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public PersonWithSignificantControlDtoBuilder withData(PersonWithSignificantControlDataDto data) {
        this.data = data;
        return this;
    }

    public PersonWithSignificantControlDto build() {
        PersonWithSignificantControlDto dto = new PersonWithSignificantControlDto();
        dto.setId(id);
        dto.setData(data);
        return dto;
    }

    public static class DataBuilder {
        private final PersonWithSignificantControlDataDto data = new PersonWithSignificantControlDataDto();

        public DataBuilder withKind(String kind) {
            data.setKind(kind);
            return this;
        }
        public DataBuilder withAppointmentId(String appointmentId) {
            data.setAppointmentId(appointmentId);
            return this;
        }
        public DataBuilder withCountry(Country country) {
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
        public DataBuilder withNaturesOfControl(List<NatureOfControl> naturesOfControl) {
            data.setNaturesOfControl(naturesOfControl);
            return this;
        }
        public DataBuilder withServiceAddress(AddressDto serviceAddress) {
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
        public DataBuilder withNationality1(Nationality nationality1) {
            data.setNationality1(nationality1);
            return this;
        }
        public DataBuilder withNationality2(Nationality nationality2) {
            data.setNationality2(nationality2);
            return this;
        }
        public DataBuilder withUsualResidentialAddress(AddressDto usualResidentialAddress) {
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
        public DataBuilder withLegalEntityRegistrationLocation(Country legalEntityRegistrationLocation) {
            data.setLegalEntityRegistrationLocation(legalEntityRegistrationLocation);
            return this;
        }
        public DataBuilder withRegisteredCompanyNumber(String registeredCompanyNumber) {
            data.setRegisteredCompanyNumber(registeredCompanyNumber);
            return this;
        }
        public DataBuilder withPrincipalOfficeAddress(AddressDto principalOfficeAddress) {
            data.setPrincipalOfficeAddress(principalOfficeAddress);
            return this;
        }
        public DataBuilder withType(PersonWithSignificantControlType type) {
            data.setType(type);
            return this;
        }
        public PersonWithSignificantControlDataDto build() {
            return data;
        }
    }
}
