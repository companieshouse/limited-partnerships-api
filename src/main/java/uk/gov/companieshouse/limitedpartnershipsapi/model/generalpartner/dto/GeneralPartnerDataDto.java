package uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDataDto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class GeneralPartnerDataDto extends PartnerDataDto {

    // Person
    public static final String SERVICE_ADDRESS_FIELD = "service_address";

    // Legal Entity
    public static final String NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD = "not_disqualified_statement_checked";

    @JsonInclude(NON_NULL)
    @JsonProperty(NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD)
    private Boolean notDisqualifiedStatementChecked;

    public Boolean getNotDisqualifiedStatementChecked() {
        return notDisqualifiedStatementChecked;
    }

    public void setNotDisqualifiedStatementChecked(Boolean notDisqualifiedStatementChecked) {
        this.notDisqualifiedStatementChecked = notDisqualifiedStatementChecked;
    }
}
