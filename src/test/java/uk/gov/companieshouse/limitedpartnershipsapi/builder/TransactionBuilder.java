package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;

public class TransactionBuilder {
    public static final String TRANSACTION_ID = "863851-951242-143528";
    public static final String SUBMISSION_ID = LimitedPartnershipBuilder.SUBMISSION_ID;

    private IncorporationKind incorporationKind = IncorporationKind.REGISTRATION;
    private String kind = FILING_KIND_LIMITED_PARTNERSHIP;
    String uri = String.format("/transactions/%s/limited-partnership/partnership/%s",
            TRANSACTION_ID,
            SUBMISSION_ID
    );

    public TransactionBuilder forPartner(String kind, String url, String id) {
        this.kind = kind;
        this.uri = String.format(url, TRANSACTION_ID, id);

        return this;
    }

    public TransactionBuilder withIncorporationKind(IncorporationKind incorporationKind) {
        this.incorporationKind = incorporationKind;
        return this;
    }

    public Transaction build() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        transaction.setCompanyName("Test Partnership");
        transaction.setCompanyNumber("LP123456");
        transaction.setFilingMode(incorporationKind.getDescription());

        Resource resource = new Resource();
        resource.setKind(kind);

        Map<String, String> links = new HashMap<>();
        links.put("resource", uri);
        resource.setLinks(links);

        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(uri, resource);
        transaction.setResources(resourceMap);

        return transaction;
    }
}
