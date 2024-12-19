package uk.gov.companieshouse.limitedpartnershipsapi.utils;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.transaction.Transaction;

import java.util.Objects;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_RESOURCE;

@Component
public class TransactionUtils {

    public boolean isTransactionLinkedToLimitedPartnershipSubmission(Transaction transaction, String limitedPartnershipSubmissionSelfLink) {
        if (StringUtils.isBlank(limitedPartnershipSubmissionSelfLink)) {
            return false;
        }

        if (Objects.isNull(transaction) || Objects.isNull(transaction.getResources())) {
            return false;
        }

        return transaction.getResources().entrySet().stream()
                .filter(resource -> FILING_KIND_LIMITED_PARTNERSHIP.equals(resource.getValue().getKind()))
                .anyMatch(resource -> limitedPartnershipSubmissionSelfLink.equals(resource.getValue().getLinks().get(LINK_RESOURCE)));
    }
}
