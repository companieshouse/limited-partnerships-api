package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import uk.gov.companieshouse.limitedpartnershipsapi.model.LimitedPartnerType;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class LimitedPartnerDto {
    @JsonInclude(NON_NULL)
    @JsonProperty("data")
    @Valid
    private LimitedPartnerDataDto data;

    @JsonInclude(NON_NULL)
    @JsonProperty("partner_type")
    private LimitedPartnerType partnerType;

    public LimitedPartnerDataDto getData() {
        return data;
    }

    public void setData(LimitedPartnerDataDto data) {
        this.data = data;
    }

    public LimitedPartnerType getPartnerType() {
        return partnerType;
    }

    public void setPartnerType(LimitedPartnerType partnerType) {
        this.partnerType = partnerType;
    }

    public class LimitedPartnerData {
        private PartnerType partnerType;

        public PartnerType getPartnerType() {
            return partnerType;
        }

        public void setPartnerType(PartnerType partnerType) {
            this.partnerType = partnerType;
        }
    }

    public class PartnerType {
        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}