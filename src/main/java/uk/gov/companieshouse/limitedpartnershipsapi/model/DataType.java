package uk.gov.companieshouse.limitedpartnershipsapi.model;

public enum DataType {
    EMAIL("email");

    private final String description;

    DataType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}