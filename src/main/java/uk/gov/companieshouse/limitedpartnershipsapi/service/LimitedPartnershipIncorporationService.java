package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipIncorporationMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.IncorporationSubResourcesDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipIncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_REGISTRATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;

@Service
public class LimitedPartnershipIncorporationService {

    private LimitedPartnershipService limitedPartnershipService;

    private final LimitedPartnershipIncorporationRepository repository;
    private final TransactionService transactionService;

    private final LimitedPartnershipIncorporationMapper mapper;

    private final TransactionUtils transactionUtils;

    public LimitedPartnershipIncorporationService(
            LimitedPartnershipService limitedPartnershipService,
            LimitedPartnershipIncorporationRepository repository,
            LimitedPartnershipIncorporationMapper mapper,
            TransactionUtils transactionUtils,
            TransactionService transactionService) {
        this.limitedPartnershipService = limitedPartnershipService;
        this.repository = repository;
        this.mapper = mapper;
        this.transactionUtils = transactionUtils;
        this.transactionService = transactionService;
    }

    public String createIncorporation(Transaction transaction, String requestId, String userId)
            throws ServiceException {
        var dao = new LimitedPartnershipIncorporationDao();
        dao.getData().setKind(FILING_KIND_REGISTRATION);
        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);

        LimitedPartnershipIncorporationDao insertedIncorporation = repository.insert(dao);

        transaction.setFilingMode(FILING_KIND_REGISTRATION);

        String incorporationUri = getSubmissionUri(transaction.getId(), insertedIncorporation.getId());
        updateIncorporationTypeWithSelfLink(dao, incorporationUri);

        updateTransactionWithIncorporationResource(transaction,
                incorporationUri, requestId);

        return insertedIncorporation.getId();
    }

    private void updateTransactionWithIncorporationResource(Transaction transaction, String incorporationUri, String loggingContext)
            throws ServiceException {
        var incorporationTransactionResource = createIncorporationTransactionResource(incorporationUri);

        // TODO set filing_mode on transaction (requires update to SDK)
        transaction.setResources(Collections.singletonMap(incorporationUri, incorporationTransactionResource));
        transactionService.updateTransaction(transaction, loggingContext);
    }

    private Resource createIncorporationTransactionResource(String incorporationUri) {
        var incorporationResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", incorporationUri);

        incorporationResource.setLinks(linksMap);
        incorporationResource.setKind(FILING_KIND_REGISTRATION);

        return incorporationResource;
    }


    public LimitedPartnershipIncorporationDto getIncorporation(Transaction transaction,
                                                               String filingResourceId,
                                                               boolean includeSubResources) throws ServiceException {
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

            // TODO Set collections of actual General Partners and Limited Partners once implemented
            subResourcesDto.setGeneralPartners(new ArrayList<>());
            subResourcesDto.setLimitedPartners(new ArrayList<>());

            LimitedPartnershipSubmissionDto partnershipDto = limitedPartnershipService.getLimitedPartnership(transaction);
            subResourcesDto.setPartnership(partnershipDto);

            incorporationDto.setSubResources(subResourcesDto);
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
