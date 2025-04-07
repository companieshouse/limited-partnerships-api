package uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.validator.EnumValid;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class IncorporationDataDto {

    @JsonInclude(NON_NULL)
    @JsonProperty("kind")
    @EnumValid(message = "Kind must be valid")
    @NotNull(message = "Kind must not be null")
    private IncorporationKind kind;

    public String getKind() {
        return kind != null ? kind.getDescription() : null;
    }

    public void setKind(IncorporationKind kind) {
        this.kind = kind;
    }
}