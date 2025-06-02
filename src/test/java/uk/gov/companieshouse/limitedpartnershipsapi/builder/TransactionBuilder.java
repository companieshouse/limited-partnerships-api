package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;

public class TransactionBuilder {
    public static final String TRANSACTION_ID = "863851-951242-143528";
    public static final String SUBMISSION_ID = "abc-123";

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

    public Transaction build() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

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
