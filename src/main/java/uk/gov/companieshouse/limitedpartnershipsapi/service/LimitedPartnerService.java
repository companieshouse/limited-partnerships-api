package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.time.LocalDateTime;
import java.util.Collections;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@Service
public class LimitedPartnerService {

    private final LimitedPartnerRepository repository;

    private final LimitedPartnerMapper mapper;

    public LimitedPartnerService(
            LimitedPartnerRepository repository,
            LimitedPartnerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public String createLimitedPartner(Transaction transaction, LimitedPartnerDto limitedPartnerDto, String requestId, String userId) throws ServiceException {
        LimitedPartnerDao dao = mapper.dtoToDao(limitedPartnerDto);
        dao.getData().setKind(FILING_KIND_LIMITED_PARTNER);
        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);

        LimitedPartnerDao insertedSubmission = repository.insert(dao);

        ApiLogger.infoContext(requestId, String.format("Limited Partner submission created with id: %s", insertedSubmission.getId()));

        transaction.setFilingMode(FILING_KIND_LIMITED_PARTNER);

        final String submissionUri = getSubmissionUri(transaction.getId(), insertedSubmission.getId());
        updateLimitedPartnerTypeWithSelfLink(dao, submissionUri);

        return insertedSubmission.getId();
    }

    private void updateLimitedPartnerTypeWithSelfLink(LimitedPartnerDao limitedPartnerDao,
                                                      String submissionUri) {
        limitedPartnerDao.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        repository.save(limitedPartnerDao);
    }

    private String getSubmissionUri(String transactionId, String submissionId) {
        return String.format(URL_GET_LIMITED_PARTNER, transactionId, submissionId);
    }
}
