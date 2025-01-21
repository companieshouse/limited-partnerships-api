package uk.gov.companieshouse.limitedpartnershipsapi.service;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;

import java.time.LocalDateTime;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;

@Service
public class LimitedPartnershipIncorporationService {

    public static final String LIMITED_PARTNERSHIP_REGISTRATION_KIND = "limited-partnership-registration";
    private final LimitedPartnershipIncorporationRepository repository;

    public LimitedPartnershipIncorporationService(
            LimitedPartnershipIncorporationRepository repository) {
        this.repository = repository;
    }

    public String createIncorporationType(String userId, String transaction) {
        var dao = new LimitedPartnershipIncorporationDao();
        dao.getData().setKind(LIMITED_PARTNERSHIP_REGISTRATION_KIND);
        // TODO set etag
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);
        LimitedPartnershipIncorporationDao insertedIncorporation = repository.insert(dao);

        String incorporationUri = getSubmissionUri(transaction, insertedIncorporation.getId());
        updateIncorporationTypeWithSelfLink(dao, incorporationUri);

        // TODO Update transaction with master_resource
        return insertedIncorporation.getId();
    }

    private void updateIncorporationTypeWithSelfLink(LimitedPartnershipIncorporationDao incorporationDao,
            String submissionUri) {
        incorporationDao.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        repository.save(incorporationDao);
    }

    private String getSubmissionUri(String transactionId, String submissionId) {
        return String.format(URL_GET_INCORPORATION, transactionId, submissionId);
    }
}
