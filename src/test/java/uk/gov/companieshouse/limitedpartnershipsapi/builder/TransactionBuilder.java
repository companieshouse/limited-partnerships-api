package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionLinks;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_COSTS;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_RESOURCE;

public class TransactionBuilder {
    public static final String TRANSACTION_ID = "863851-951242-143528";
    public static final String SUBMISSION_ID = LimitedPartnershipBuilder.SUBMISSION_ID;
    public static final String COMPANY_NUMBER = "LP123456";

    private FilingMode filingMode = FilingMode.REGISTRATION;
    private String kind = FILING_KIND_LIMITED_PARTNERSHIP;

    String transactionUri = String.format("/transactions/%s", TRANSACTION_ID);
    String transactionPaymentUri = null;
    String uri = String.format("/transactions/%s/limited-partnership/partnership/%s",
            TRANSACTION_ID,
            SUBMISSION_ID
    );
    String costLink;

    public TransactionBuilder forPartner(String kind, String url, String id) {
        this.kind = kind;
        this.uri = String.format(url, TRANSACTION_ID, id);

        return this;
    }

    public TransactionBuilder withIncorporationKind(FilingMode filingMode) {
        this.filingMode = filingMode;
        return this;
    }

    public TransactionBuilder withFilingMode(String filingMode) {
        this.filingMode = FilingMode.fromDescription(filingMode);
        return this;
    }

    public TransactionBuilder withPayment() {
        this.transactionPaymentUri = String.format("%s/payment", transactionUri);
        return this;
    }

    public TransactionBuilder withResource(String uri) {
        this.uri = uri;
        return this;
    }

    public TransactionBuilder withCostLink(String costLink) {
        this.costLink = costLink;
        return this;
    }

    public Transaction build() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        transaction.setCompanyName("Test Partnership");
        transaction.setCompanyNumber(COMPANY_NUMBER);
        transaction.setFilingMode(filingMode.getDescription());

        TransactionLinks transactionLinks = new TransactionLinks();
        transactionLinks.setSelf(transactionUri);
        if (transactionPaymentUri != null) {
            transactionLinks.setPayment(transactionPaymentUri);
        }
        transaction.setLinks(transactionLinks);

        Resource resource = new Resource();
        resource.setKind(kind);

        Map<String, String> resourceLinks = new HashMap<>();
        resourceLinks.put(LINK_RESOURCE, uri);
        resource.setLinks(resourceLinks);
        if (costLink != null) {
            resourceLinks.put(LINK_COSTS, costLink);
        }

        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(uri, resource);
        transaction.setResources(resourceMap);

        return transaction;
    }
}
