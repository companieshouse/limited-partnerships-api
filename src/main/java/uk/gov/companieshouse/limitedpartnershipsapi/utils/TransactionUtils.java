package uk.gov.companieshouse.limitedpartnershipsapi.utils;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.transaction.Transaction;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.IncorporationKind.REGISTRATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.IncorporationKind.TRANSITION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_RESOURCE;

@Component
public class TransactionUtils {

    public boolean isTransactionLinkedToLimitedPartnershipSubmission(Transaction transaction, String limitedPartnershipSubmissionSelfLink) {
        return doChecks(transaction, limitedPartnershipSubmissionSelfLink, FILING_KIND_LIMITED_PARTNERSHIP);
    }

    public boolean doesTransactionHaveALimitedPartnershipSubmission(Transaction transaction) {
        if (Objects.isNull(transaction) || Objects.isNull(transaction.getResources())) {
            return false;
        }

        Optional<?> optionalEntry = transaction.getResources().entrySet().stream()
                .filter(resource -> FILING_KIND_LIMITED_PARTNERSHIP.equals(resource.getValue().getKind()))
                .findFirst();

        return optionalEntry.isPresent();
    }

    public boolean isTransactionLinkedToLimitedPartnershipIncorporation(Transaction transaction, String limitedPartnershipIncorporationSelfLink) {
        return doIncorporationChecks(transaction, limitedPartnershipIncorporationSelfLink);
    }

    public boolean isTransactionLinkedToPartnerSubmission(Transaction transaction, String partnerSubmissionSelfLink, String kind) {
        return doChecks(transaction, partnerSubmissionSelfLink, kind);
    }

    private boolean doChecks(Transaction transaction, String selfLink, String kind) {
        if (!isTransactionAndSelfLinkValid(transaction, selfLink)) {
            return false;
        }

        return transaction.getResources().entrySet().stream()
                .filter(resource -> kind.equals(resource.getValue().getKind()))
                .anyMatch(resource -> selfLink.equals(resource.getValue().getLinks().get(LINK_RESOURCE)));
    }

    private boolean doIncorporationChecks(Transaction transaction, String selfLink) {
        if (!isTransactionAndSelfLinkValid(transaction, selfLink)) {
            return false;
        }

        return transaction.getResources().entrySet().stream()
                .filter(resource -> (
                        REGISTRATION.getDescription().equals(resource.getValue().getKind()))
                        || TRANSITION.getDescription().equals(resource.getValue().getKind()))
                .anyMatch(resource -> selfLink.equals(resource.getValue().getLinks().get(LINK_RESOURCE)));
    }

    private boolean isTransactionAndSelfLinkValid(Transaction transaction, String selfLink) {
        if (StringUtils.isBlank(selfLink)) {
            return false;
        }

        return !(Objects.isNull(transaction) || Objects.isNull(transaction.getResources()));
    }
}
