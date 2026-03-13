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

public record PscDataDto(
    @JsonProperty("appointment_id") String appointmentId,
    @JsonProperty("country") Country country,
    @JsonProperty("date_effective_from") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dateEffectiveFrom,
    @JsonProperty("date_of_birth") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dateOfBirth,
    @JsonProperty("etag") String etag,
    @JsonProperty("forename") String forename,
    @JsonProperty("former_names") String formerNames,
    @JsonProperty("governing_law") String governingLaw,
    @JsonProperty("kind") String kind,
    @JsonProperty("legal_entity_name") String legalEntityName,
    @JsonProperty("legal_entity_register_name") String legalEntityRegisterName,
    @JsonProperty("legal_entity_registration_location") Country legalEntityRegistrationLocation,
    @JsonProperty("legal_form") String legalForm,
    @JsonInclude(Include.NON_NULL) @JsonProperty("legal_personality_statement_checked") Boolean legalPersonalityStatementChecked,
    @JsonProperty("nationality1") Nationality nationality1,
    @JsonProperty("nationality2") Nationality nationality2,
    @JsonProperty("natures_of_control") List<NatureOfControl> naturesOfControl,
    @JsonProperty("principal_office_address") AddressDto principalOfficeAddress,
    @JsonProperty("registered_company_number") String registeredCompanyNumber,
    @JsonProperty("resignation_date") LocalDate resignationDate,
    @JsonProperty("service_address") AddressDto serviceAddress,
    @JsonProperty("surname") String surname,
    @JsonProperty("usual_residential_address") AddressDto usualResidentialAddress
) {}
