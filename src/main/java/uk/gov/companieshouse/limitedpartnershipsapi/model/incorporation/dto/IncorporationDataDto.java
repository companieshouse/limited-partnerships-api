package uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.validator.EnumValid;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class IncorporationDataDto {

    @JsonInclude(NON_NULL)
    @JsonProperty("kind")
    @EnumValid(message = "Kind must be valid")
    @NotNull(message = "Kind must not be null")
    private FilingMode kind;

    public String getKind() {
        return kind != null ? kind.getDescription() : null;
    }

    public void setKind(FilingMode kind) {
        this.kind = kind;
    }
}