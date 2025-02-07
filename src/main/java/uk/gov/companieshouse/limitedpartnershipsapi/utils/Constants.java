package uk.gov.companieshouse.limitedpartnershipsapi.utils;

public class Constants {

    // Request header names
    public static final String ERIC_REQUEST_ID_KEY = "X-Request-Id";
    // URL path parameters
    public static final String URL_PARAM_TRANSACTION_ID = "transactionId";
    public static final String URL_PARAM_SUBMISSION_ID = "submissionId";
    public static final String URL_PARAM_FILING_RESOURCE_ID = "filingResourceId";
    public static final String TRANSACTION_KEY = "transaction";
    // URIs
    public static final String TRANSACTIONS_PRIVATE_API_URI_PREFIX = "/private/transactions/";
    public static final String SUBMISSION_URI_PATTERN = "/transactions/%s/limited-partnership/%s";
    public static final String URL_GET_PARTNERSHIP = "/transactions/%s/limited-partnership/partnership/%s";
    public static final String URL_GET_INCORPORATION = "/transactions/%s/incorporation/limited-partnership/%s";
    public static final String URL_GET_LIMITED_PARTNER = "/transactions/%s/limited-partnership/limited-partner/%s";
    // Filings
    public static final String FILING_KIND_REGISTRATION = "limited-partnership-registration";
    public static final String FILING_KIND_LIMITED_PARTNERSHIP = "limited-partnership";
    public static final String FILING_KIND_LIMITED_PARTNER = "limited-partner";
    public static final String LINK_SELF = "self";
    public static final String LINK_RESOURCE = "resource";
    // Validation rules
    public static final int MIN_SIZE = 1;
    public static final String MIN_SIZE_MESSAGE = "must be greater than {min}";
    public static final int MAX_SIZE = 50;
    public static final String MAX_SIZE_MESSAGE = "must be less than {max}";
    private Constants() {
    }
}
