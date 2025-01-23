package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipIncorporationMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.IncorporationSubResourcesDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipIncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;

@Service
public class LimitedPartnershipIncorporationService {

    public static final String LIMITED_PARTNERSHIP_REGISTRATION_KIND = "limited-partnership-registration";

    private LimitedPartnershipService limitedPartnershipService;

    private final LimitedPartnershipIncorporationRepository repository;

    private final LimitedPartnershipIncorporationMapper mapper;

    private final TransactionUtils transactionUtils;

    public LimitedPartnershipIncorporationService(
            LimitedPartnershipService limitedPartnershipService,
            LimitedPartnershipIncorporationRepository repository,
            LimitedPartnershipIncorporationMapper mapper,
            TransactionUtils transactionUtils) {
        this.limitedPartnershipService = limitedPartnershipService;
        this.repository = repository;
        this.mapper = mapper;
        this.transactionUtils = transactionUtils;
    }

    public String createIncorporation(String userId, String transaction) {
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


    public LimitedPartnershipIncorporationDto getIncorporation(Transaction transaction,
                                                               String filingResourceId,
                                                               boolean includeSubResources) throws ResourceNotFoundException {
        String submissionUri = getSubmissionUri(transaction.getId(), filingResourceId);
        if (!transactionUtils.isTransactionLinkedToLimitedPartnershipIncorporation(transaction, submissionUri)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches incorporation id: %s", transaction.getId(), filingResourceId));
        }

        var incorporation = repository.findById(filingResourceId);
        LimitedPartnershipIncorporationDao incorporationDao = incorporation.orElseThrow(() -> new ResourceNotFoundException(String.format("Incorporation with id %s not found", filingResourceId)));

        LimitedPartnershipIncorporationDto incorporationDto = mapper.daoToDto(incorporationDao);

        if (includeSubResources) {
            IncorporationSubResourcesDto subResourcesDto = new IncorporationSubResourcesDto();

            Resource partnershipResource = transaction.getResources().values().stream()
                    .filter(resource -> FILING_KIND_LIMITED_PARTNERSHIP.equals(resource.getKind()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(String.format(
                            "Incorporation id: %s does not have a partnership resource associated with the transaction", filingResourceId)));

            Map.Entry<String, String> linkEntry = partnershipResource.getLinks().entrySet().stream()
                    .filter(entry -> entry.getKey().equals("resource"))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(String.format(
                            "Incorporation id: %s does not have a partnership resource with a 'resource' link within the transaction", filingResourceId)));

            String link = linkEntry.getValue();
            String submissionId = link.substring(link.lastIndexOf('/') + 1);

            System.out.println("\n\n*** pship sub id = " + submissionId + " *** \n\n");

            LimitedPartnershipSubmissionDto partnershipDto = limitedPartnershipService.getLimitedPartnership(transaction, submissionId);
            subResourcesDto.setPartnership(partnershipDto);
            incorporationDto.setSubResources(subResourcesDto);

            // TODO Add collections of General Partners and Limited Partners once implemented
        }

        return incorporationDto;
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
