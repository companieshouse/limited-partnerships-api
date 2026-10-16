package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartnership;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipPatchDto;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PartnershipServiceUpdateTest extends AbstractPartnershipServiceTestSupport {
	Transaction transaction = new TransactionBuilder().build();

	private static final String USER_ID = "xbJf0l";
	private static final String REQUEST_ID = "fd4gld5h3jhh";
	private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;

	@Autowired
	private PartnershipService partnershipService;

	@MockitoBean
	private PartnershipRepository partnershipRepository;

	@Captor
	private ArgumentCaptor<PartnershipDao> submissionCaptor;

	@Test
	void shouldUpdateTheDao() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException, ApiErrorResponseException, URIValidationException {
		mockTransactionServicePatchRequest(transaction);

		PartnershipDao partnershipDao = new PartnershipBuilder().withId(PartnershipBuilder.SUBMISSION_ID).buildDao();
		partnershipDao.setUpdatedBy(USER_ID);
		partnershipDao.setTransactionId(transaction.getId());

		PartnershipPatchDto limitedPartnershipPatchDto = new PartnershipPatchDto();
		limitedPartnershipPatchDto.setPartnershipName("Test Partnership Updated");

		when(partnershipRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDao));

		// dao partnership name before mapping/update
		assertThat(partnershipDao.getData().getPartnershipName()).isEqualTo("Test Partnership");

		partnershipService.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

		verify(partnershipRepository).save(submissionCaptor.capture());

		PartnershipDao sentSubmission = submissionCaptor.getValue();

		assertThat(sentSubmission.getUpdatedBy()).isEqualTo(USER_ID);
		assertThat(sentSubmission.getData().getPartnershipName()).isEqualTo("Test Partnership Updated");

		// copyMetaDataForUpdate
		assertThat(sentSubmission.getId()).isEqualTo(partnershipDao.getId());
		assertThat(sentSubmission.getCreatedAt()).isEqualTo(partnershipDao.getCreatedAt());
		assertThat(sentSubmission.getUpdatedBy()).isEqualTo(partnershipDao.getUpdatedBy());
		assertThat(sentSubmission.getLinks()).isEqualTo(partnershipDao.getLinks());
		assertThat(sentSubmission.getTransactionId()).isEqualTo(transaction.getId());

		// setAuditDetailsForUpdate
		assertThat(sentSubmission.getUpdatedBy()).isEqualTo(USER_ID);
	}

}
