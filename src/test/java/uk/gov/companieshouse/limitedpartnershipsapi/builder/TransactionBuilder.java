package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;

public class TransactionBuilder {
    public static final String TRANSACTION_ID = "863851-951242-143528";
    public static final String SUBMISSION_ID = "abc-123";

    public Transaction build() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        Resource resource = new Resource();
        resource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);

        String uri = String.format("/transactions/%s/limited-partnership/partnership/%s",
                TRANSACTION_ID,
                SUBMISSION_ID
        );

        Map<String, String> links = new HashMap<>();
        links.put("resource", uri);
        resource.setLinks(links);

        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(uri, resource);
        transaction.setResources(resourceMap);

        return transaction;
    }

    public Transaction build(String kind, String url, String id) {
        Transaction trx = new Transaction();
        trx.setId(TRANSACTION_ID);

        Resource resource = new Resource();
        resource.setKind(kind);

        String uri = String.format(url, TRANSACTION_ID, id);

        Map<String, String> links = new HashMap<>();
        links.put("resource", uri);
        resource.setLinks(links);

        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(uri, resource);
        trx.setResources(resourceMap);

        return trx;
    }
}
