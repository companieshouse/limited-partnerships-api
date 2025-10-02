package uk.gov.companieshouse.limitedpartnershipsapi.utils;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;

import java.util.List;
import java.util.stream.Stream;

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
    public static final String TRANSACTION_ID_KEY = "transaction_id";

    // URIs
    public static final String TRANSACTIONS_PRIVATE_API_URI_PREFIX = "/private/transactions/";
    public static final String VALIDATION_STATUS_URI_SUFFIX = "/validation-status";
    public static final String COSTS_URI_SUFFIX = "/costs";
    public static final String URL_GET_PARTNERSHIP = "/transactions/%s/limited-partnership/partnership/%s";
    public static final String URL_RESUME_REGISTRATION_OR_TRANSITION = "/limited-partnerships/transaction/%s/submission/%s/resume";
    public static final String URL_RESUME_POST_TRANSITION_PARTNERSHIP = "/limited-partnerships/update/company/%s/transaction/%s/submission/%s/resume";
    public static final String URL_RESUME_POST_TRANSITION_GENERAL_PARTNER = "/limited-partnerships/update/company/%s/transaction/%s/general-partner/%s/resume";
    public static final String URL_RESUME_POST_TRANSITION_LIMITED_PARTNER = "/limited-partnerships/update/company/%s/transaction/%s/limited-partner/%s/resume";


    public static final String URL_GET_INCORPORATION = "/transactions/%s/incorporation/limited-partnership/%s";
    public static final String URL_GET_LIMITED_PARTNER = "/transactions/%s/limited-partnership/limited-partner/%s";
    public static final String URL_GET_GENERAL_PARTNER = "/transactions/%s/limited-partnership/general-partner/%s";

    // Filings
    public static final String FILING_KIND_LIMITED_PARTNERSHIP = "limited-partnership";
    public static final String FILING_KIND_LIMITED_PARTNER = "limited-partnership#limited-partner";
    public static final String FILING_KIND_GENERAL_PARTNER = "limited-partnership#general-partner";

    // Transaction links
    public static final String LINK_SELF = "self";
    public static final String LINK_RESOURCE = "resource";
    public static final String LINK_VALIDATION_STATUS = "validation_status";
    public static final String LINK_COSTS = "costs";

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

    public static final List<String> UK_COUNTRIES = List.of(
            Country.ENGLAND.getDescription(),
            Country.WALES.getDescription(),
            Country.SCOTLAND.getDescription(),
            Country.NORTHERN_IRELAND.getDescription());

    protected static final List<String> ALL_DOMESTIC_COUNTRIES = Stream.concat(
            Stream.of(Country.UNITED_KINGDOM.getDescription()),
            UK_COUNTRIES.stream()
    ).toList();

    public static final List<String> UK_POSTCODE_LETTERS_NOT_MAINLAND = List.of("JE", "GY", "IM");

    private Constants() {
    }
}
