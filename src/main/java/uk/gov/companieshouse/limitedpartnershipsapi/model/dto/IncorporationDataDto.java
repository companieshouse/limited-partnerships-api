package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import uk.gov.companieshouse.limitedpartnershipsapi.model.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.ValidEnum;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class IncorporationDataDto {

    @JsonInclude(NON_NULL)
    @JsonProperty("kind")
    @ValidEnum(message = "Kind must be valid")
    @NotNull(message = "Kind must not be null")
    private IncorporationKind kind;

    public String getKind() {
        return kind != null ? kind.getDescription() : null;
    }

    public void setKind(IncorporationKind kind) {
        this.kind = kind;
    }
}
