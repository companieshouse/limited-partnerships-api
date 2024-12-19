package uk.gov.companieshouse.limitedpartnershipsapi.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_RESOURCE;

@ExtendWith(MockitoExtension.class)
public class TransactionUtilsTest {

    private static final String LIMITED_PARTNERSHIP_SELF_LINK = "/transaction/1234/limited-partnership/partnership/1234";

    @Mock
    private Transaction transaction;

    private final TransactionUtils  transactionUtils = new TransactionUtils();

    @Test
    void givenLimitedPartnerSelfLinkIsBlank_thenReturnFalse(){
        // when
        var result = transactionUtils.isTransactionLinkedToLimitedPartnershipSubmission(transaction, "");
        // then
        assertFalse(result);
    }

    @Test
    void givenLimitedPartnerSelfLinkIsNull_thenReturnFalse(){
        // given
        when(transaction.getResources()).thenReturn(null);
        // when
        var result = transactionUtils.isTransactionLinkedToLimitedPartnershipSubmission(transaction, LIMITED_PARTNERSHIP_SELF_LINK);
        // then
        assertFalse(result);
    }


    @Test
    void givenTransactionIsNotLinkedToLimitedPartnership_thenReturnFalse(){
        // given
        Map<String, Resource> transactionResources = new HashMap<>();
        Resource limitedPartnershipResource = new Resource();
        limitedPartnershipResource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);
        Map<String, String> limitedPartnershipsResourceLinks = new HashMap<>();
        String nonMatchingResourceLink = "/transaction/1234/limited-partnership/partnership/wrong_id";
        limitedPartnershipsResourceLinks.put(LINK_RESOURCE, nonMatchingResourceLink);
        limitedPartnershipResource.setLinks(limitedPartnershipsResourceLinks);
        transactionResources.put(nonMatchingResourceLink, limitedPartnershipResource);
        when(transaction.getResources()).thenReturn(transactionResources);
        // when
        var result = transactionUtils.isTransactionLinkedToLimitedPartnershipSubmission(transaction, LIMITED_PARTNERSHIP_SELF_LINK);
        // then
        assertFalse(result);
    }

    @Test
    void givenTransactionLinkedToLimitedPartnership_thenReturnTrue(){
        // given
        Map<String, Resource> transactionResources = new HashMap<>();
        Resource limitedPartnershipResource = new Resource();
        limitedPartnershipResource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);
        Map<String, String> limitedPartnershipsResourceLinks = new HashMap<>();
        limitedPartnershipsResourceLinks.put(LINK_RESOURCE, LIMITED_PARTNERSHIP_SELF_LINK);
        limitedPartnershipResource.setLinks(limitedPartnershipsResourceLinks);
        transactionResources.put(LIMITED_PARTNERSHIP_SELF_LINK, limitedPartnershipResource);
        when(transaction.getResources()).thenReturn(transactionResources);
        // when
        var result = transactionUtils.isTransactionLinkedToLimitedPartnershipSubmission(transaction, LIMITED_PARTNERSHIP_SELF_LINK);
        // then
        assertTrue(result);
    }
}
