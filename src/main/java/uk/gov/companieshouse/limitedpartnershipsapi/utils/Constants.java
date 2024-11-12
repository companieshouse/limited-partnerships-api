package uk.gov.companieshouse.limitedpartnershipsapi.utils;

public class Constants {

    private Constants() { }

    // Request header names
    public static final String ERIC_REQUEST_ID_KEY = "X-Request-Id";
    public static final String ERIC_IDENTITY = "ERIC-identity";

    // URL path parameters
    public static final String URL_PARAM_TRANSACTION_ID = "transaction_id";

    // URLs
    public static final String ENDPOINT_PARTNERSHIP = "/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/limited-partnership/partnership";
    public static final String URL_GET_PARTNERSHIP = "/transactions/%s/limited-partnership/partnership/%s";
}
