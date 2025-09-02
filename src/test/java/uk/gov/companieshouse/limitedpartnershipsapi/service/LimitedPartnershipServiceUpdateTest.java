package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LimitedPartnershipServiceUpdateTest {

    Transaction transaction = new TransactionBuilder().build();

    private static final String USER_ID = "xbJf0l";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String SUBMISSION_ID = LimitedPartnershipBuilder.SUBMISSION_ID;

    @Autowired
    private LimitedPartnershipService service;

    @MockitoBean
    private LimitedPartnershipRepository repository;

    @MockitoBean
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<LimitedPartnershipDao> submissionCaptor;

    @Nested
    public class updateLimitedPartnership {

        @Nested
        class updatePartnershipName {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().buildDao();

                var limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setPartnershipName("Test Partnership Updated");

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));

                // dao partnership name before mapping/update
                assertEquals("Test Partnership", limitedPartnershipDao.getData().getPartnershipName());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();
                assertEquals(USER_ID, sentSubmission.getUpdatedBy());

                assertEquals("Test Partnership Updated", sentSubmission.getData().getPartnershipName());
            }

            @Test
            void shouldReturnDtoContainingPartnershipName() throws ResourceNotFoundException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().buildDao();
                limitedPartnershipDao.getData().setPartnershipName("Test Partnership Updated");

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any(), any())).thenReturn(true);

                // when
                LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(limitedPartnershipDao.getId());
                assertEquals("Test Partnership Updated", retrievedDto.getData().getPartnershipName());
            }
        }

        @Nested
        class updateRegisteredOfficeAddress {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().buildDao();

                AddressDto registeredOfficeAddress = getAddressDto();

                LimitedPartnershipPatchDto limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setRegisteredOfficeAddress(registeredOfficeAddress);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));

                // dao registered office address is null before mapping/update
                assertNull(limitedPartnershipDao.getData().getRegisteredOfficeAddress());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();

                assertEquals(registeredOfficeAddress.getAddressLine1(), sentSubmission.getData().getRegisteredOfficeAddress().getAddressLine1());
                assertEquals(registeredOfficeAddress.getCountry(), sentSubmission.getData().getRegisteredOfficeAddress().getCountry());
                assertEquals(registeredOfficeAddress.getLocality(), sentSubmission.getData().getRegisteredOfficeAddress().getLocality());
                assertEquals(registeredOfficeAddress.getPostalCode(), sentSubmission.getData().getRegisteredOfficeAddress().getPostalCode());
                assertEquals(registeredOfficeAddress.getPremises(), sentSubmission.getData().getRegisteredOfficeAddress().getPremises());
            }

            @Test
            void shouldReturnDtoContainingRegisteredOfficeAddress() throws ResourceNotFoundException {
                // given
                AddressDao registeredOfficeAddress = getAddressDao();

                LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().buildDao();
                limitedPartnershipDao.getData().setRegisteredOfficeAddress(registeredOfficeAddress);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any(), any())).thenReturn(true);

                // when
                LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(limitedPartnershipDao.getId());

                assertEquals(registeredOfficeAddress.getAddressLine1(), retrievedDto.getData().getRegisteredOfficeAddress().getAddressLine1());
                assertEquals(registeredOfficeAddress.getCountry(), retrievedDto.getData().getRegisteredOfficeAddress().getCountry());
                assertEquals(registeredOfficeAddress.getLocality(), retrievedDto.getData().getRegisteredOfficeAddress().getLocality());
                assertEquals(registeredOfficeAddress.getPostalCode(), retrievedDto.getData().getRegisteredOfficeAddress().getPostalCode());
                assertEquals(registeredOfficeAddress.getPremises(), retrievedDto.getData().getRegisteredOfficeAddress().getPremises());
            }
        }

        @Nested
        class UpdateTerm {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().buildDao();
                limitedPartnershipDao.getData().setTerm(null);

                LimitedPartnershipPatchDto limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setTerm(Term.BY_AGREEMENT);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));

                // dao term is null before mapping/update
                assertNull(limitedPartnershipDao.getData().getTerm());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();

                assertEquals(Term.BY_AGREEMENT, sentSubmission.getData().getTerm());
            }

            @Test
            void shouldReturnDtoContainingRegisteredOfficeAddress() throws ResourceNotFoundException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().buildDao();
                limitedPartnershipDao.getData().setTerm(Term.BY_AGREEMENT);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any(), any())).thenReturn(true);

                // when
                LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(limitedPartnershipDao.getId());

                assertEquals(Term.BY_AGREEMENT, retrievedDto.getData().getTerm());

            }
        }

        @Nested
        class updatePrincipalPlaceOfBusinessAddress {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().buildDao();

                AddressDto principalPlaceOfBusinessAddress = getAddressDto();

                LimitedPartnershipPatchDto limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setPrincipalPlaceOfBusinessAddress(principalPlaceOfBusinessAddress);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));

                // dao principal place of business address is null before mapping/update
                assertNull(limitedPartnershipDao.getData().getPrincipalPlaceOfBusinessAddress());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();

                assertEquals(principalPlaceOfBusinessAddress.getAddressLine1(), sentSubmission.getData().getPrincipalPlaceOfBusinessAddress().getAddressLine1());
                assertEquals(principalPlaceOfBusinessAddress.getCountry(), sentSubmission.getData().getPrincipalPlaceOfBusinessAddress().getCountry());
                assertEquals(principalPlaceOfBusinessAddress.getLocality(), sentSubmission.getData().getPrincipalPlaceOfBusinessAddress().getLocality());
                assertEquals(principalPlaceOfBusinessAddress.getPostalCode(), sentSubmission.getData().getPrincipalPlaceOfBusinessAddress().getPostalCode());
                assertEquals(principalPlaceOfBusinessAddress.getPremises(), sentSubmission.getData().getPrincipalPlaceOfBusinessAddress().getPremises());
            }

            @Test
            void shouldReturnDtoContainingRegisteredOfficeAddress() throws ResourceNotFoundException {
                // given
                AddressDao principalPlaceOfBusinessAddress = getAddressDao();

                LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().buildDao();
                limitedPartnershipDao.getData().setPrincipalPlaceOfBusinessAddress(principalPlaceOfBusinessAddress);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any(), any())).thenReturn(true);

                // when
                LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(limitedPartnershipDao.getId());

                assertEquals(principalPlaceOfBusinessAddress.getAddressLine1(), retrievedDto.getData().getPrincipalPlaceOfBusinessAddress().getAddressLine1());
                assertEquals(principalPlaceOfBusinessAddress.getCountry(), retrievedDto.getData().getPrincipalPlaceOfBusinessAddress().getCountry());
                assertEquals(principalPlaceOfBusinessAddress.getLocality(), retrievedDto.getData().getPrincipalPlaceOfBusinessAddress().getLocality());
                assertEquals(principalPlaceOfBusinessAddress.getPostalCode(), retrievedDto.getData().getPrincipalPlaceOfBusinessAddress().getPostalCode());
                assertEquals(principalPlaceOfBusinessAddress.getPremises(), retrievedDto.getData().getPrincipalPlaceOfBusinessAddress().getPremises());
            }
        }

        @Nested
        class updateSicCode {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().buildDao();
                limitedPartnershipDao.getData().setSicCodes(null);

                List<String> sicCodes = Arrays.asList("12A45", "22345", "33345");

                LimitedPartnershipPatchDto limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setSicCodes(sicCodes);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any(), any())).thenReturn(true);

                // dao sic codes is null before mapping/update
                assertNull(limitedPartnershipDao.getData().getSicCodes());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();

                assertEquals(sicCodes, sentSubmission.getData().getSicCodes());
            }

            @Test
            void shouldReturnDtoContainingSicCodes() throws ResourceNotFoundException {
                // given
                List<String> sicCodes = Arrays.asList("12345", "22345", "33345");

                LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().buildDao();
                limitedPartnershipDao.getData().setSicCodes(sicCodes);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any(), any())).thenReturn(true);

                // when
                LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(limitedPartnershipDao.getId());

                assertEquals(sicCodes, retrievedDto.getData().getSicCodes());
            }
        }

        @Nested
        class UpdateLawfulPurposeStatementChecked {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().buildDao();
                limitedPartnershipDao.getData().setLawfulPurposeStatementChecked(null);

                LimitedPartnershipPatchDto limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setLawfulPurposeStatementChecked(Boolean.TRUE);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any(), any())).thenReturn(true);

                // dao lawful purpose statement check is null before mapping/update
                assertNull(limitedPartnershipDao.getData().getLawfulPurposeStatementChecked());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();

                assertTrue(sentSubmission.getData().getLawfulPurposeStatementChecked());
            }

            @Test
            void shouldReturnDtoContainingLawfulPurposeStatementChecked() throws ResourceNotFoundException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().buildDao();
                limitedPartnershipDao.getData().setLawfulPurposeStatementChecked(true);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any(), any())).thenReturn(true);

                // when
                LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(limitedPartnershipDao.getId());

                assertTrue(retrievedDto.getData().getLawfulPurposeStatementChecked());
            }
        }
    }

    private static AddressDao getAddressDao() {
        LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().withAddresses().buildDao();
        return limitedPartnershipDao.getData().getRegisteredOfficeAddress();
    }

    private static AddressDto getAddressDto() {
        LimitedPartnershipDto limitedPartnershipDao = new LimitedPartnershipBuilder().withAddresses().buildDto();
        return limitedPartnershipDao.getData().getPrincipalPlaceOfBusinessAddress();
    }
}
