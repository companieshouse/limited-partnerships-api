package uk.gov.companieshouse.limitedpartnershipsapi.utils;

public class Constants {
    // Request header names
    public static final String ERIC_REQUEST_ID_KEY = "X-Request-Id";

    // URL path parameters
    public static final String URL_PARAM_TRANSACTION_ID = "transactionId";
    public static final String URL_PARAM_SUBMISSION_ID = "submissionId";
    public static final String URL_PARAM_FILING_RESOURCE_ID = "filingResourceId";
    public static final String URL_PARAM_INCORPORATION_ID = "incorporationId";
    public static final String URL_PARAM_GENERAL_PARTNER_ID = "generalPartnerId";
    public static final String URL_PARAM_LIMITED_PARTNER_ID = "limitedPartnerId";
    public static final String TRANSACTION_KEY = "transaction";

    // URIs
    public static final String TRANSACTIONS_PRIVATE_API_URI_PREFIX = "/private/transactions/";
    public static final String VALIDATION_STATUS_URI_SUFFIX = "/validation-status";
    public static final String URL_GET_PARTNERSHIP = "/transactions/%s/limited-partnership/partnership/%s";
    public static final String URL_RESUME_PARTNERSHIP = "/limited-partnerships/registration/transaction/%s/submission/%s/which-type";
    public static final String URL_GET_INCORPORATION = "/transactions/%s/incorporation/limited-partnership/%s";
    public static final String URL_GET_LIMITED_PARTNER = "/transactions/%s/limited-partnership/limited-partner/%s";
    public static final String URL_GET_GENERAL_PARTNER = "/transactions/%s/limited-partnership/general-partner/%s";

    // Filings
    public static final String FILING_KIND_LIMITED_PARTNERSHIP = "limited-partnership";
    public static final String FILING_KIND_LIMITED_PARTNER = "limited-partnership#limited-partner";
    public static final String FILING_KIND_GENERAL_PARTNER = "limited-partnership#general-partner";
    public static final String LINK_SELF = "self";
    public static final String LINK_RESOURCE = "resource";

    // Validation rules
    public static final int MIN_SIZE = 1;
    public static final String MIN_SIZE_MESSAGE = "must be greater than {min}";
    public static final int POSTAL_CODE_MAX_SIZE = 15;
    public static final int SHORT_MAX_SIZE = 50;
    public static final int LONG_MAX_SIZE = 160;
    public static final int PREMISES_MAX_SIZE = 200;
    public static final String MAX_SIZE_MESSAGE = "must be less than {max}";
    public static final String REG_EXP_FOR_ALLOWED_CHARACTERS = "^[-,.:; 0-9A-Z&@$£¥€'\"«»?!/\\\\()\\[\\]{}<>*=#%+ÀÁÂÃÄÅĀĂĄÆǼÇĆĈĊČÞĎÐÈÉÊËĒĔĖĘĚĜĞĠĢĤĦÌÍÎÏĨĪĬĮİĴĶĹĻĽĿŁÑŃŅŇŊÒÓÔÕÖØŌŎŐǾŒŔŖŘŚŜŞŠŢŤŦÙÚÛÜŨŪŬŮŰŲŴẀẂẄỲÝŶŸŹŻŽa-zſƒǺàáâãäåāăąæǽçćĉċčþďðèéêëēĕėęěĝģğġĥħìíîïĩīĭįĵķĺļľŀłñńņňŋòóôõöøōŏőǿœŕŗřśŝşšţťŧùúûüũūŭůűųŵẁẃẅỳýŷÿźżž]*$";

    public static final String INVALID_CHARACTERS_MESSAGE = "must only include letters a to z, numbers and common special characters such as hyphens, spaces and apostrophes";

    public static final String LIMITED_PARTNERSHIP_FIELD = "limited_partnership";
    public static final String GENERAL_PARTNER_FIELD = "general_partners";
    public static final String LIMITED_PARTNER_FIELD = "limited_partners";


    private Constants() {
    }
}
