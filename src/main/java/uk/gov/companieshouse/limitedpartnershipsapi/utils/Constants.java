package uk.gov.companieshouse.limitedpartnershipsapi.utils;

public class Constants {

    private Constants() {
    }

    // Request header names
    public static final String ERIC_REQUEST_ID_KEY = "X-Request-Id";

    // URL path parameters
    public static final String URL_PARAM_TRANSACTION_ID = "transactionId";
    public static final String URL_PARAM_SUBMISSION_ID = "submissionId";

    public static final String TRANSACTION_KEY = "transaction";

    // URIs
    public static final String TRANSACTIONS_PRIVATE_API_URI_PREFIX = "/private/transactions/";
    public static final String SUBMISSION_URI_PATTERN = "/transactions/%s/limited-partnership/%s";
    public static final String URL_GET_PARTNERSHIP = "/transactions/%s/limited-partnership/partnership/%s";

    // Filings
    public static final String FILING_KIND_LIMITED_PARTNERSHIP = "limited-partnership";

    public static final String LINK_SELF = "self";
    public static final String LINK_RESOURCE = "resource";
}
