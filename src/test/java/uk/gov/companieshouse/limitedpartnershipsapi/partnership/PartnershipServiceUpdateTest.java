package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.TransactionService;

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
class PartnershipServiceUpdateTest {

    Transaction transaction = new TransactionBuilder().build();

    private static final String USER_ID = "xbJf0l";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;

    @Autowired
    private PartnershipService service;

    @MockitoBean
    private PartnershipRepository repository;

    @MockitoBean
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<PartnershipDao> submissionCaptor;

    @Nested
    public class updateLimitedPartnership {

        @Nested
        class updatePartnershipName {
            @Test
            void shouldUpdateTheDao() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
                // given
                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

                var limitedPartnershipPatchDto = new PartnershipPatchDto();
                limitedPartnershipPatchDto.setPartnershipName("Test Partnership Updated");

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(
                    partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // dao partnership name before mapping/update
                assertEquals("Test Partnership", partnershipDao.getData().getPartnershipName());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                PartnershipDao sentSubmission = submissionCaptor.getValue();
                assertEquals(USER_ID, sentSubmission.getUpdatedBy());

                assertEquals("Test Partnership Updated", sentSubmission.getData().getPartnershipName());
            }

            @Test
            void shouldReturnDtoContainingPartnershipName() throws ResourceNotFoundException {
                // given
                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
                partnershipDao.getData().setPartnershipName("Test Partnership Updated");

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(
                    partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // when
                PartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(partnershipDao.getId());
                assertEquals("Test Partnership Updated", retrievedDto.getData().getPartnershipName());
            }
        }

        @Nested
        class updateRegisteredOfficeAddress {
            @Test
            void shouldUpdateTheDao() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
                // given
                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

                AddressDto registeredOfficeAddress = getAddressDto();

                PartnershipPatchDto partnershipPatchDto = new PartnershipPatchDto();
                partnershipPatchDto.setRegisteredOfficeAddress(registeredOfficeAddress);

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(
                    partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // dao registered office address is null before mapping/update
                assertNull(partnershipDao.getData().getRegisteredOfficeAddress());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, partnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                PartnershipDao sentSubmission = submissionCaptor.getValue();

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

                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
                partnershipDao.getData().setRegisteredOfficeAddress(registeredOfficeAddress);

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(
                    partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // when
                PartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(partnershipDao.getId());

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
            void shouldUpdateTheDao() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
                // given
                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
                partnershipDao.getData().setTerm(null);

                PartnershipPatchDto partnershipPatchDto = new PartnershipPatchDto();
                partnershipPatchDto.setTerm(Term.BY_AGREEMENT);

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(
                    partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // dao term is null before mapping/update
                assertNull(partnershipDao.getData().getTerm());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, partnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                PartnershipDao sentSubmission = submissionCaptor.getValue();

                assertEquals(Term.BY_AGREEMENT, sentSubmission.getData().getTerm());
            }

            @Test
            void shouldReturnDtoContainingRegisteredOfficeAddress() throws ResourceNotFoundException {
                // given
                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
                partnershipDao.getData().setTerm(Term.BY_AGREEMENT);

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(
                    partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // when
                PartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(partnershipDao.getId());

                assertEquals(Term.BY_AGREEMENT, retrievedDto.getData().getTerm());

            }
        }

        @Nested
        class updatePrincipalPlaceOfBusinessAddress {
            @Test
            void shouldUpdateTheDao() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
                // given
                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

                AddressDto principalPlaceOfBusinessAddress = getAddressDto();

                PartnershipPatchDto partnershipPatchDto = new PartnershipPatchDto();
                partnershipPatchDto.setPrincipalPlaceOfBusinessAddress(principalPlaceOfBusinessAddress);

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(
                    partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // dao principal place of business address is null before mapping/update
                assertNull(partnershipDao.getData().getPrincipalPlaceOfBusinessAddress());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, partnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                PartnershipDao sentSubmission = submissionCaptor.getValue();

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

                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
                partnershipDao.getData().setPrincipalPlaceOfBusinessAddress(principalPlaceOfBusinessAddress);

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(
                    partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // when
                PartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(partnershipDao.getId());

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
            void shouldUpdateTheDao() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
                // given
                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
                partnershipDao.getData().setSicCodes(null);

                List<String> sicCodes = Arrays.asList("12345", "22345", "33345");

                PartnershipPatchDto partnershipPatchDto = new PartnershipPatchDto();
                partnershipPatchDto.setSicCodes(sicCodes);

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // dao sic codes is null before mapping/update
                assertNull(partnershipDao.getData().getSicCodes());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, partnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                PartnershipDao sentSubmission = submissionCaptor.getValue();

                assertEquals(sentSubmission.getData().getSicCodes().size(), sicCodes.size());
                assertEquals(sicCodes.getFirst().toString(), sentSubmission.getData().getSicCodes().getFirst().toString());
            }

            @Test
            void shouldReturnDtoContainingSicCodes() throws ResourceNotFoundException {
                // given
                List<String> sicCodes = Arrays.asList("12345", "22345", "33345");

                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
                partnershipDao.getData().setSicCodes(sicCodes);

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // when
                PartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(partnershipDao.getId());

                assertEquals(sicCodes, retrievedDto.getData().getSicCodes());
            }
        }

        @Nested
        class UpdateLawfulPurposeStatementChecked {
            @Test
            void shouldUpdateTheDao() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
                // given
                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
                partnershipDao.getData().setLawfulPurposeStatementChecked(null);

                PartnershipPatchDto partnershipPatchDto = new PartnershipPatchDto();
                partnershipPatchDto.setLawfulPurposeStatementChecked(Boolean.TRUE);

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // dao lawful purpose statement check is null before mapping/update
                assertNull(partnershipDao.getData().getLawfulPurposeStatementChecked());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, partnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                PartnershipDao sentSubmission = submissionCaptor.getValue();

                assertTrue(sentSubmission.getData().getLawfulPurposeStatementChecked());
            }

            @Test
            void shouldReturnDtoContainingLawfulPurposeStatementChecked() throws ResourceNotFoundException {
                // given
                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
                partnershipDao.getData().setLawfulPurposeStatementChecked(true);

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // when
                PartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(partnershipDao.getId());

                assertTrue(retrievedDto.getData().getLawfulPurposeStatementChecked());
            }
        }

        @Nested
        class UpdateHasPersonWithSignificantControl {
            @Test
            void shouldUpdateTheDao() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
                // given
                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
                partnershipDao.getData().setHasPersonWithSignificantControl(null);

                PartnershipPatchDto partnershipPatchDto = new PartnershipPatchDto();
                partnershipPatchDto.setHasPersonWithSignificantControl(Boolean.TRUE);

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // dao has person with significant control is null before mapping/update
                assertNull(partnershipDao.getData().getHasPersonWithSignificantControl());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, partnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                PartnershipDao sentSubmission = submissionCaptor.getValue();

                assertTrue(sentSubmission.getData().getHasPersonWithSignificantControl());
            }

            @Test
            void shouldReturnDtoContainingHasPersonWithSignificantControl() throws ResourceNotFoundException {
                // given
                PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
                partnershipDao.getData().setHasPersonWithSignificantControl(true);

                when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(partnershipDao));
                when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

                // when
                PartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(partnershipDao.getId());

                assertTrue(retrievedDto.getData().getHasPersonWithSignificantControl());
            }
        }
    }

    private static AddressDao getAddressDao() {
        PartnershipDao partnershipDao = new PartnershipBuilder().withAddresses().buildDao();
        return partnershipDao.getData().getRegisteredOfficeAddress();
    }

    private static AddressDto getAddressDto() {
        PartnershipDto limitedPartnershipDao = new PartnershipBuilder().withAddresses().buildDto();
        return limitedPartnershipDao.getData().getPrincipalPlaceOfBusinessAddress();
    }
}
