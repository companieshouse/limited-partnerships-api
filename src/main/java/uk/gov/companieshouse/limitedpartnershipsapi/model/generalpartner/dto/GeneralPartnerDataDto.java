package uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDataDto;

public class GeneralPartnerDataDto extends PartnerDataDto {

    // Person
    public static final String SERVICE_ADDRESS_FIELD = "service_address";

    @JsonProperty(SERVICE_ADDRESS_FIELD)
    @Valid
    private AddressDto serviceAddress;

    public AddressDto getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(AddressDto serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    // Legal Entity
    public static final String NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD = "not_disqualified_statement_checked";

    @JsonProperty(NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD)
    private Boolean notDisqualifiedStatementChecked;

    public boolean isLegalEntity() {
        return getLegalEntityRegisterName() != null || getLegalForm() != null;
    }

    public Boolean getNotDisqualifiedStatementChecked() {
        return notDisqualifiedStatementChecked;
    }

    public void setNotDisqualifiedStatementChecked(Boolean notDisqualifiedStatementChecked) {
        this.notDisqualifiedStatementChecked = notDisqualifiedStatementChecked;
    }
}
