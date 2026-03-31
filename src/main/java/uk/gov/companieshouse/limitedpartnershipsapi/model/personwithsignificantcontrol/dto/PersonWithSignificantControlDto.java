package uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class PersonWithSignificantControlDto {
    protected String id;

    @JsonInclude(NON_NULL)
    @JsonProperty("data")
    private PersonWithSignificantControlDataDto data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PersonWithSignificantControlDataDto getData() {
        return data;
    }

    public void setData(PersonWithSignificantControlDataDto data) {
        this.data = data;
    }
}
