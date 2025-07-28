package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.privatetransaction.PrivateTransactionResourceHandler;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionDeleteResource;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionPatch;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertEquals;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind.REGISTRATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_RESOURCE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_RESUME;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceTest {

    private static final String TRANSACTION_ID = "12345678";
    private static final String RESOURCE_ID = "resource1234";
    private static final String LOGGING_CONTEXT = "fg4536";
    private static final String PRIVATE_TRANSACTIONS_URL = "/private/transactions/";
    private static final String INCORPORATION_SELF_LINK = "/transactions/txn-123/incorporation/limited-partnership/sub-456";
    private static final String SUBMISSION_ID = LimitedPartnershipBuilder.SUBMISSION_ID;
    private static final String LIMITED_PARTNERSHIP_SELF_LINK = "/transaction/1234/limited-partnership/partnership/1234";
    private static final String LIMITED_PARTNER_SELF_LINK = "/transactions/txn-123/limited-partnership/limited-partner/sub-456";
    private static final String GENERAL_PARTNER_SELF_LINK = "/transactions/trans123/limited-partnership/generalPartner/gp123";

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateTransactionResourceHandler privateTransactionResourceHandler;

    @Mock
    private PrivateTransactionPatch privateTransactionPatch;

    @Mock
    private PrivateTransactionDeleteResource privateTransactionDeleteResource;

    @Mock
    private ApiResponse<Void> apiPatchResponse;

    @Mock
    private ApiResponse<Void> apiDeleteResponse;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;

    @BeforeEach
    void init() {
        transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
    }

    @Test
    void testServiceExceptionThrownWhenApiClientSdkThrowsURIValidationException() throws IOException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.patch(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenThrow(new URIValidationException("ERROR"));

        assertThrows(ServiceException.class, () -> transactionService.updateTransaction(transaction, LOGGING_CONTEXT));
    }

    @Test
    void testServiceExceptionThrownWhenApiClientSdkThrowsIOException() throws IOException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.patch(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));

        assertThrows(ServiceException.class, () -> transactionService.updateTransaction(transaction, LOGGING_CONTEXT));
    }

    @Test
    void testServiceExceptionThrownWhenApiClientSdkReturnsAnInvalidHttpCode() throws IOException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.patch(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenReturn(apiPatchResponse);
        when(apiPatchResponse.getStatusCode()).thenReturn(400);

        assertThrows(ServiceException.class, () -> transactionService.updateTransaction(transaction, LOGGING_CONTEXT));
    }

    @Test
    void testUpdatingATransactionIsSuccessful() throws IOException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.patch(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenReturn(apiPatchResponse);
        when(apiPatchResponse.getStatusCode()).thenReturn(204);

        try {
            transactionService.updateTransaction(transaction, LOGGING_CONTEXT);
        } catch (Exception e) {
            fail("Should not throw exception");
        }
    }

    @Test
    void testUpdatingATransactionNameIsSuccessful() throws IOException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.patch(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenReturn(apiPatchResponse);
        when(apiPatchResponse.getStatusCode()).thenReturn(204);

        try {
            transactionService.updateTransactionWithPartnershipName(transaction, "New name", LOGGING_CONTEXT);
        } catch (Exception e) {
            fail("Should not throw exception");
        }
    }

    @Test
    void testDeleteTransactionResourceIsSuccessful() throws IOException, URIValidationException, ServiceException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.delete(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID + "/resources", RESOURCE_ID))
                .thenReturn(privateTransactionDeleteResource);
        when(privateTransactionDeleteResource.execute()).thenReturn(apiDeleteResponse);
        when(apiDeleteResponse.getStatusCode()).thenReturn(204);

        transactionService.deleteTransactionResource(TRANSACTION_ID, RESOURCE_ID, LOGGING_CONTEXT);

        assertDoesNotThrow(() -> transactionService.deleteTransactionResource(TRANSACTION_ID, RESOURCE_ID, LOGGING_CONTEXT));
    }

    @Test
    void testDeleteTransactionResourceThrowsServiceExceptionOnURIValidationException() throws IOException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.delete(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID + "/resources", RESOURCE_ID))
                .thenReturn(privateTransactionDeleteResource);
        when(privateTransactionDeleteResource.execute()).thenThrow(new URIValidationException("ERROR"));

        assertThatThrownBy(() -> transactionService.deleteTransactionResource(TRANSACTION_ID, RESOURCE_ID, LOGGING_CONTEXT))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Error deleting resource resource1234 from transaction 12345678");
    }

    @Test
    void testDeleteTransactionResourceThrowsServiceExceptionOnIOException() throws IOException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.delete(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID + "/resources", RESOURCE_ID))
                .thenReturn(privateTransactionDeleteResource);
        when(privateTransactionDeleteResource.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));

        assertThatThrownBy(() -> transactionService.deleteTransactionResource(TRANSACTION_ID, RESOURCE_ID, LOGGING_CONTEXT))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Error deleting resource resource1234 from transaction 12345678");
    }

    @Test
    void testDeleteTransactionResourceThrowsServiceExceptionOnInvalidStatusCode() throws IOException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.delete(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID + "/resources", RESOURCE_ID))
                .thenReturn(privateTransactionDeleteResource);
        when(privateTransactionDeleteResource.execute()).thenReturn(apiDeleteResponse);
        when(apiDeleteResponse.getStatusCode()).thenReturn(400);

        assertThatThrownBy(() -> transactionService.deleteTransactionResource(TRANSACTION_ID, RESOURCE_ID, LOGGING_CONTEXT))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Error deleting resource resource1234 from transaction 12345678");
    }

    @ParameterizedTest
    @EnumSource(value = IncorporationKind.class, names = {
            "REGISTRATION",
            "TRANSITION"
    })
    void shouldAddCorrectLinksToTransactionResource(IncorporationKind incoporationKind) throws Exception {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.patch(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenReturn(apiPatchResponse);
        when(apiPatchResponse.getStatusCode()).thenReturn(204);

        transaction.setFilingMode(incoporationKind.getDescription());
        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipDto();
        limitedPartnershipDto.setData(new DataDto());

        String submissionUri = String.format(URL_GET_PARTNERSHIP, transaction.getId(), SUBMISSION_ID);
        var limitedPartnershipResource = new Resource();
        Map<String, String> linksMap = new HashMap<>();
        linksMap.put(LINK_RESOURCE, submissionUri);
        limitedPartnershipResource.setLinks(linksMap);
        limitedPartnershipResource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);

        transactionService.updateTransactionWithLinksAndPartnershipName(
                transaction,
                limitedPartnershipDto,
                submissionUri,
                limitedPartnershipResource,
                "",
                SUBMISSION_ID
        );

        // assert transaction resources are updated appropriately
        assertEquals(limitedPartnershipDto.getData().getPartnershipName(), transaction.getCompanyName());
        assertNull(transaction.getCompanyNumber());

        // assert transaction resources are updated appropriately
        assertEquals(submissionUri, transaction.getResources().get(submissionUri).getLinks().get("resource"));

        Map<String, Resource> transactionResources = transaction.getResources();
        assertEquals(1, transactionResources.size());
        assertThat(transactionResources.values())
                .allSatisfy(resource -> assertThat(resource.getLinks())
                        .hasSize(1)
                        .isNotNull()
                        .containsKeys(LINK_RESOURCE));

        // assert resume link is correct
        String resumeUri = String.format(URL_RESUME, transaction.getId(), SUBMISSION_ID);
        assertEquals(resumeUri, transaction.getResumeJourneyUri());
    }

    @Test
    void testCreateLimitedPartnershipTransactionResource() {
        String submissionUri = String.format(URL_GET_PARTNERSHIP, transaction.getId(), SUBMISSION_ID);
        Resource resource = transactionService.createLimitedPartnershipTransactionResource(submissionUri);
        assertEquals(submissionUri, resource.getLinks().get(LINK_RESOURCE));
        assertEquals(FILING_KIND_LIMITED_PARTNERSHIP, resource.getKind());
    }

    @Test
    void testHasExistingPartnershipWhenKindIsPresent() {
        String submissionUri = String.format(URL_GET_PARTNERSHIP, transaction.getId(), SUBMISSION_ID);
        var limitedPartnershipResource = new Resource();
        Map<String, String> linksMap = new HashMap<>();
        linksMap.put(LINK_RESOURCE, submissionUri);
        limitedPartnershipResource.setLinks(linksMap);
        limitedPartnershipResource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);
        transaction.setResources(Collections.singletonMap(submissionUri, limitedPartnershipResource));
        assertTrue(transactionService.hasExistingLimitedPartnership(transaction));
    }

    @Test
    void testDoesNotHaveExistingPartnershipWhenResourceIsNull() {
        assertFalse(transactionService.hasExistingLimitedPartnership(transaction));
    }

    @Test
    void testDoesNotHaveExistingPartnershipWhenKindIsNotInTheMap() {
        String submissionUri = String.format(URL_GET_PARTNERSHIP, transaction.getId(), SUBMISSION_ID);
        var limitedPartnershipResource = new Resource();
        transaction.setResources(Collections.singletonMap(submissionUri, limitedPartnershipResource));
        assertFalse(transactionService.hasExistingLimitedPartnership(transaction));
    }

    @Test
    void givenTransactionIsNotLinkedToLimitedPartnershipIncorporationDueToIncorrectFilingKind_thenReturnFalse() {
        // given + when
        var result = testIfTransactionIsLinkedToLimitedPartnershipIncorporation(FILING_KIND_LIMITED_PARTNERSHIP);
        // then
        assertFalse(result);
    }

    @ParameterizedTest
    @EnumSource(value = IncorporationKind.class, names = {
            "REGISTRATION",
            "TRANSITION"
    })
    void givenTransactionIsLinkedToLimitedPartnershipRegistrationIncorporation_thenReturnTrue(IncorporationKind incoporationKind) {
        // given + when
        var result = testIfTransactionIsLinkedToLimitedPartnershipIncorporation(incoporationKind.getDescription());
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
        transaction.setResources(transactionResources);
        // when
        var result = transactionService.doesTransactionHaveALimitedPartnership(transaction);
        // then
        assertTrue(result);
    }

    @Test
    void givenTransactionHasALimitedPartnershipCalledWithNullTransaction_thenReturnFalse() {
        // when
        var result = transactionService.doesTransactionHaveALimitedPartnership(null);
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
        transaction.setResources(transactionResources);
        // when
        var result = transactionService.doesTransactionHaveALimitedPartnership(transaction);
        // then
        assertFalse(result);
    }

    @Test
    void givenLimitedPartnerSelfLinkIsBlank_thenReturnFalse() {
        // when
        var result = transactionService.isTransactionLinkedToLimitedPartnership(transaction, LIMITED_PARTNER_SELF_LINK);
        // then
        assertFalse(result);
    }

    @Test
    void givenLimitedPartnerSelfLinkIsNull_thenReturnFalse() {
        // given
        transaction.setResources(null);
        // when
        var result = transactionService.isTransactionLinkedToLimitedPartnership(transaction, LIMITED_PARTNERSHIP_SELF_LINK);
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
        transaction.setResources(transactionResources);
        // when
        var result = transactionService.isTransactionLinkedToLimitedPartnership(transaction, LIMITED_PARTNERSHIP_SELF_LINK);
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
        transaction.setResources(transactionResources);
        // when
        var result = transactionService.isTransactionLinkedToLimitedPartnership(transaction, LIMITED_PARTNERSHIP_SELF_LINK);
        // then
        assertTrue(result);
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
        transaction.setResources(transactionResources);
        // when
        var result = transactionService.isTransactionLinkedToLimitedPartnership(transaction, LIMITED_PARTNER_SELF_LINK);
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
        transaction.setResources(transactionResources);
        // when
        var result = transactionService.isTransactionLinkedToPartner(transaction, LIMITED_PARTNER_SELF_LINK, FILING_KIND_LIMITED_PARTNER);
        // then
        assertTrue(result);
    }

    @Test
    void givenALimitedPartnerSelfLinkIsBlank_thenReturnFalse() {
        // when
        var result = transactionService.isTransactionLinkedToPartner(transaction, "", FILING_KIND_LIMITED_PARTNER);
        // then
        assertFalse(result);
    }

    @Test
    void givenALimitedPartnerSelfLinkIsNull_thenReturnFalse() {
        // given
        transaction.setResources(null);
        // when
        var result = transactionService.isTransactionLinkedToPartner(transaction, LIMITED_PARTNER_SELF_LINK, FILING_KIND_LIMITED_PARTNER);
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

        transaction.setResources(transactionResources);
        var result = transactionService.isTransactionLinkedToPartner(transaction, GENERAL_PARTNER_SELF_LINK, FILING_KIND_GENERAL_PARTNER);

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

        transaction.setResources(transactionResources);
        var result = transactionService.isTransactionLinkedToPartner(transaction, GENERAL_PARTNER_SELF_LINK, FILING_KIND_GENERAL_PARTNER);

        assertFalse(result);
    }

    @Test
    void givenGeneralPartnerSelfLinkIsBlank_thenReturnFalse() {
        var result = transactionService.isTransactionLinkedToPartner(transaction, "", FILING_KIND_GENERAL_PARTNER);
        assertFalse(result);
    }

    @Test
    void givenGeneralPartnerTransactionIsNull_thenReturnFalse() {
        when(transaction.getResources()).thenReturn(null);
        var result = transactionService.isTransactionLinkedToPartner(transaction, GENERAL_PARTNER_SELF_LINK, FILING_KIND_GENERAL_PARTNER);
        assertFalse(result);
    }

    private boolean testIfTransactionIsLinkedToLimitedPartnershipIncorporation(String kind) {
        // given
        Map<String, Resource> transactionResources = new HashMap<>();
        Resource limitedPartnershipResource = new Resource();
        limitedPartnershipResource.setKind(kind);
        Map<String, String> limitedPartnershipsResourceLinks = new HashMap<>();
        limitedPartnershipsResourceLinks.put(LINK_RESOURCE, INCORPORATION_SELF_LINK);
        limitedPartnershipResource.setLinks(limitedPartnershipsResourceLinks);
        transactionResources.put(INCORPORATION_SELF_LINK, limitedPartnershipResource);
        transaction.setResources(transactionResources);
        // when
        return transactionService.isTransactionLinkedToLimitedPartnershipIncorporation(transaction, INCORPORATION_SELF_LINK);
    }
}
