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
import static uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind.REGISTRATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_RESOURCE;

@ExtendWith(MockitoExtension.class)
class TransactionUtilsTest {

    private static final String LIMITED_PARTNERSHIP_SELF_LINK = "/transaction/1234/limited-partnership/partnership/1234";
    private static final String LIMITED_PARTNER_SELF_LINK = "/transactions/txn-123/limited-partnership/limited-partner/sub-456";
    private static final String GENERAL_PARTNER_SELF_LINK = "/transactions/trans123/limited-partnership/generalPartner/gp123";

    private final TransactionUtils transactionUtils = new TransactionUtils();

    @Mock
    private Transaction transaction;

    @Test
    void givenLimitedPartnerSelfLinkIsBlank_thenReturnFalse() {
        // when
        var result = transactionUtils.isTransactionLinkedToLimitedPartnership(transaction, LIMITED_PARTNER_SELF_LINK);
        // then
        assertFalse(result);
    }

    @Test
    void givenLimitedPartnerSelfLinkIsNull_thenReturnFalse() {
        // given
        when(transaction.getResources()).thenReturn(null);
        // when
        var result = transactionUtils.isTransactionLinkedToLimitedPartnership(transaction, LIMITED_PARTNERSHIP_SELF_LINK);
        // then
        assertFalse(result);
    }

    @Test
    void givenTransactionIsNotLinkedToLimitedPartnership_thenReturnFalse() {
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
        var result = transactionUtils.isTransactionLinkedToLimitedPartnership(transaction, LIMITED_PARTNERSHIP_SELF_LINK);
        // then
        assertFalse(result);
    }

    @Test
    void givenTransactionLinkedToLimitedPartnership_thenReturnTrue() {
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
        var result = transactionUtils.isTransactionLinkedToLimitedPartnership(transaction, LIMITED_PARTNERSHIP_SELF_LINK);
        // then
        assertTrue(result);
    }

    @Test
    void givenTransactionHasALimitedPartnership_thenReturnTrue() {
        // given
        Map<String, Resource> transactionResources = new HashMap<>();
        Resource limitedPartnershipResource = new Resource();
        limitedPartnershipResource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);
        transactionResources.put(LIMITED_PARTNERSHIP_SELF_LINK, limitedPartnershipResource);
        when(transaction.getResources()).thenReturn(transactionResources);
        // when
        var result = transactionUtils.doesTransactionHaveALimitedPartnership(transaction);
        // then
        assertTrue(result);
    }

    @Test
    void givenTransactionHasALimitedPartnershipCalledWithNullTransaction_thenReturnFalse() {
        // when
        var result = transactionUtils.doesTransactionHaveALimitedPartnership(null);
        // then
        assertFalse(result);
    }

    @Test
    void givenTransactionHasALimitedPartnershipNotFound_thenReturnFalse() {
        // given
        Map<String, Resource> transactionResources = new HashMap<>();
        Resource limitedPartnershipResource = new Resource();
        limitedPartnershipResource.setKind(REGISTRATION.getDescription());
        transactionResources.put(LIMITED_PARTNERSHIP_SELF_LINK, limitedPartnershipResource);
        when(transaction.getResources()).thenReturn(transactionResources);
        // when
        var result = transactionUtils.doesTransactionHaveALimitedPartnership(transaction);
        // then
        assertFalse(result);
    }

    @Test
    void givenTransactionIsNotLinkedToLimitedPartner_thenReturnFalse() {
        // given
        Map<String, Resource> transactionResources = new HashMap<>();
        Resource limitedPartnershipResource = new Resource();
        limitedPartnershipResource.setKind(FILING_KIND_LIMITED_PARTNER);
        Map<String, String> limitedPartnershipsResourceLinks = new HashMap<>();
        String nonMatchingResourceLink = "/transactions/txn-123/limited-partnership/limited-partner/wrong-id";
        limitedPartnershipsResourceLinks.put(LINK_RESOURCE, nonMatchingResourceLink);
        limitedPartnershipResource.setLinks(limitedPartnershipsResourceLinks);
        transactionResources.put(nonMatchingResourceLink, limitedPartnershipResource);
        when(transaction.getResources()).thenReturn(transactionResources);
        // when
        var result = transactionUtils.isTransactionLinkedToLimitedPartnership(transaction, LIMITED_PARTNER_SELF_LINK);
        // then
        assertFalse(result);
    }

    @Test
    void givenTransactionLinkedToLimitedPartner_thenReturnTrue() {
        // given
        Map<String, Resource> transactionResources = new HashMap<>();
        Resource limitedPartnerResource = new Resource();
        limitedPartnerResource.setKind(FILING_KIND_LIMITED_PARTNER);
        Map<String, String> limitedPartnerResourceLinks = new HashMap<>();
        limitedPartnerResourceLinks.put(LINK_RESOURCE, LIMITED_PARTNER_SELF_LINK);
        limitedPartnerResource.setLinks(limitedPartnerResourceLinks);
        transactionResources.put(LIMITED_PARTNER_SELF_LINK, limitedPartnerResource);
        when(transaction.getResources()).thenReturn(transactionResources);
        // when
        var result = transactionUtils.isTransactionLinkedToPartner(transaction, LIMITED_PARTNER_SELF_LINK, FILING_KIND_LIMITED_PARTNER);
        // then
        assertTrue(result);
    }

    @Test
    void givenALimitedPartnerSelfLinkIsBlank_thenReturnFalse() {
        // when
        var result = transactionUtils.isTransactionLinkedToPartner(transaction, "", FILING_KIND_LIMITED_PARTNER);
        // then
        assertFalse(result);
    }

    @Test
    void givenALimitedPartnerSelfLinkIsNull_thenReturnFalse() {
        // given
        when(transaction.getResources()).thenReturn(null);
        // when
        var result = transactionUtils.isTransactionLinkedToPartner(transaction, LIMITED_PARTNER_SELF_LINK, FILING_KIND_LIMITED_PARTNER);
        // then
        assertFalse(result);
    }

    @Test
    void givenTransactionIsLinkedToGeneralPartner_thenReturnTrue() {
        Map<String, Resource> transactionResources = new HashMap<>();
        Resource generalPartnerResource = new Resource();
        generalPartnerResource.setKind(FILING_KIND_GENERAL_PARTNER);
        transactionResources.put(GENERAL_PARTNER_SELF_LINK, generalPartnerResource);

        Map<String, String> generalPartnerResourceLinks = new HashMap<>();
        generalPartnerResourceLinks.put(LINK_RESOURCE, GENERAL_PARTNER_SELF_LINK);
        generalPartnerResource.setLinks(generalPartnerResourceLinks);

        when(transaction.getResources()).thenReturn(transactionResources);
        var result = transactionUtils.isTransactionLinkedToPartner(transaction, GENERAL_PARTNER_SELF_LINK, FILING_KIND_GENERAL_PARTNER);

        assertTrue(result);
    }

    @Test
    void givenTransactionIsNotLinkedToGeneralPartner_thenReturnFalse() {
        Map<String, Resource> transactionResources = new HashMap<>();
        Resource generalPartnerResource = new Resource();
        generalPartnerResource.setKind(FILING_KIND_GENERAL_PARTNER);
        transactionResources.put(GENERAL_PARTNER_SELF_LINK, generalPartnerResource);

        Map<String, String> generalPartnerResourceLinks = new HashMap<>();
        generalPartnerResourceLinks.put(LINK_RESOURCE, "some/garbage");
        generalPartnerResource.setLinks(generalPartnerResourceLinks);

        when(transaction.getResources()).thenReturn(transactionResources);
        var result = transactionUtils.isTransactionLinkedToPartner(transaction, GENERAL_PARTNER_SELF_LINK, FILING_KIND_GENERAL_PARTNER);

        assertFalse(result);
    }

    @Test
    void givenGeneralPartnerSelfLinkIsBlank_thenReturnFalse() {
        var result = transactionUtils.isTransactionLinkedToPartner(transaction, "", FILING_KIND_GENERAL_PARTNER);
        assertFalse(result);
    }

    @Test
    void givenGeneralPartnerTransactionIsNull_thenReturnFalse() {
        when(transaction.getResources()).thenReturn(null);
        var result = transactionUtils.isTransactionLinkedToPartner(transaction, GENERAL_PARTNER_SELF_LINK, FILING_KIND_GENERAL_PARTNER);
        assertFalse(result);
    }
}
