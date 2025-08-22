package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder.LIMITED_PARTNER_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@ExtendWith(MockitoExtension.class)
class LimitedPartnerServiceTest {

    private static final String USER_ID = "xbJf0l";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String LIMITED_ID = LIMITED_PARTNER_ID;

    private final Transaction transaction = new TransactionBuilder().forPartner(
            FILING_KIND_LIMITED_PARTNER,
            URL_GET_LIMITED_PARTNER,
            LIMITED_PARTNER_ID
    ).build();

    @InjectMocks
    private LimitedPartnerService limitedPartnerService;

    @Mock
    private LimitedPartnerRepository repository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private LimitedPartnerValidator limitedPartnerValidator;

    @Captor
    private ArgumentCaptor<LimitedPartnerDao> submissionCaptor;

    @Mock
    private LimitedPartnerMapper mapper;

    @Test
    void testGetLimitedPartnerSuccess() throws ServiceException {
        LimitedPartnerDao dao = new LimitedPartnerBuilder().personDao();
        LimitedPartnerDto limitedPartnerDto = new LimitedPartnerBuilder().personDto();

        when(repository.findById(LIMITED_ID))
                .thenReturn(Optional.of(dao));

        when(mapper.daoToDto(dao)).thenReturn(limitedPartnerDto);
        when(transactionService.isTransactionLinkedToPartner(any(Transaction.class), any(String.class), any(String.class))).thenReturn(true)
                .thenReturn(true);

        var dto = limitedPartnerService.getLimitedPartner(transaction, LIMITED_ID);
        assertEquals(limitedPartnerDto.getData().getForename(), dto.getData().getForename());
        assertEquals(limitedPartnerDto.getData().getSurname(), dto.getData().getSurname());
    }

    @Test
    void testGetLimitedPartnerNotFound() {
        when(repository.findById(LIMITED_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> limitedPartnerService.getLimitedPartner(transaction, LIMITED_ID));

        assertEquals("Limited partner submission with id " + LIMITED_PARTNER_ID + " not found", resourceNotFoundException.getMessage());
    }

    @Test
    void testCreateLinksForLimitedPartnerIsSuccessful() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        LimitedPartnerDto limitedPartnerDto = new LimitedPartnerBuilder().personDto();
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();

        when(mapper.dtoToDao(limitedPartnerDto)).thenReturn(limitedPartnerDao);
        when(repository.insert(limitedPartnerDao)).thenReturn(limitedPartnerDao);

        String submissionId = limitedPartnerService.createLimitedPartner(transaction, limitedPartnerDto, REQUEST_ID, USER_ID);

        verify(mapper, times(1)).dtoToDao(limitedPartnerDto);
        verify(repository, times(1)).insert(limitedPartnerDao);
        verify(repository, times(1)).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();
        assertEquals(USER_ID, sentSubmission.getCreatedBy());
        assertEquals(FILING_KIND_LIMITED_PARTNER, sentSubmission.getData().getKind());
        assertEquals(LIMITED_ID, submissionId);

        String expectedUri = String.format(URL_GET_LIMITED_PARTNER, transaction.getId(), LIMITED_ID);
        assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
    }

    @Test
    void testGetLimitedPartnerDataList() {
        LimitedPartnerDto limitedPartnerDto = new LimitedPartnerBuilder().personDto();
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();

        when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(TransactionBuilder.TRANSACTION_ID)).thenReturn(List.of(limitedPartnerDao));
        when(mapper.daoToDto(any(LimitedPartnerDao.class))).thenReturn(limitedPartnerDto);

        List<LimitedPartnerDataDto> limitedPartnerDataDtoList = limitedPartnerService.getLimitedPartnerDataList(transaction);
        assertEquals(1, limitedPartnerDataDtoList.size());
    }

    @Test
    void testGetLimitedPartnerList() throws ServiceException {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();
        LimitedPartnerDto limitedPartnerDto = new LimitedPartnerBuilder().personDto();

        List<LimitedPartnerDao> limitedPartnerDaos = List.of(limitedPartnerDao, limitedPartnerDao);
        when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(TransactionBuilder.TRANSACTION_ID)).thenReturn(limitedPartnerDaos);

        List<LimitedPartnerDto> limitedPartnerDtos = List.of(limitedPartnerDto, limitedPartnerDto);
        when(mapper.daoToDto(limitedPartnerDaos.get(0))).thenReturn(limitedPartnerDtos.get(0));
        when(mapper.daoToDto(limitedPartnerDaos.get(1))).thenReturn(limitedPartnerDtos.get(1));

        List<LimitedPartnerDto> limitedPartnerDtoList = limitedPartnerService.getLimitedPartnerList(transaction);

        assertThat(limitedPartnerDtoList).containsExactly(limitedPartnerDtos.get(0), limitedPartnerDtos.get(1));
    }

    @Test
    void testGetLimitedPartnerEmptyList() {
        when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(TransactionBuilder.TRANSACTION_ID)).thenReturn(Collections.emptyList());

        List<LimitedPartnerDataDto> limitedPartnerDtoList = limitedPartnerService.getLimitedPartnerDataList(transaction);

        assertEquals(0, limitedPartnerDtoList.size());
    }
}
