package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipIncorporationMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.IncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.IncorporationSubResourcesDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.LimitedPartnershipIncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_COSTS;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_RESOURCE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_VALIDATION_STATUS;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.COSTS_URI_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.VALIDATION_STATUS_URI_SUFFIX;

@Service
public class LimitedPartnershipIncorporationService {

    private final GeneralPartnerService generalPartnerService;

    private final LimitedPartnerService limitedPartnerService;

    private final LimitedPartnershipService limitedPartnershipService;

    private final LimitedPartnershipIncorporationRepository repository;
    private final TransactionService transactionService;

    private final LimitedPartnershipIncorporationMapper mapper;

    private final TransactionUtils transactionUtils;

    public LimitedPartnershipIncorporationService(
            GeneralPartnerService generalPartnerService,
            LimitedPartnerService limitedPartnerService,
            LimitedPartnershipService limitedPartnershipService,
            LimitedPartnershipIncorporationRepository repository,
            LimitedPartnershipIncorporationMapper mapper,
            TransactionUtils transactionUtils,
            TransactionService transactionService) {
        this.generalPartnerService = generalPartnerService;
        this.limitedPartnerService = limitedPartnerService;
        this.limitedPartnershipService = limitedPartnershipService;
        this.repository = repository;
        this.mapper = mapper;
        this.transactionUtils = transactionUtils;
        this.transactionService = transactionService;
    }

    public String createIncorporation(Transaction transaction, IncorporationDto incorporationDto, String requestId, String userId)
            throws ServiceException {
        final var kind = incorporationDto.getData().getKind();

        var dao = new LimitedPartnershipIncorporationDao();
        dao.getData().setKind(kind);
        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedBy(userId);

        LimitedPartnershipIncorporationDao insertedIncorporation = repository.insert(dao);

        transaction.setFilingMode(kind);

        String incorporationUri = getSubmissionUri(transaction.getId(), insertedIncorporation.getId());
        updateIncorporationTypeWithSelfLink(dao, incorporationUri);

        updateTransactionWithIncorporationResource(transaction, incorporationUri, kind, requestId);

        return insertedIncorporation.getId();
    }

    private void updateTransactionWithIncorporationResource(Transaction transaction, String incorporationUri, String kind, String loggingContext)
            throws ServiceException {
        var incorporationTransactionResource = createIncorporationTransactionResource(incorporationUri, kind, transaction);

        transaction.setResources(Collections.singletonMap(incorporationUri, incorporationTransactionResource));
        transactionService.updateTransaction(transaction, loggingContext);
    }

    private Resource createIncorporationTransactionResource(String incorporationUri, String kind, Transaction transaction) {
        var incorporationResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put(LINK_RESOURCE, incorporationUri);
        linksMap.put(LINK_VALIDATION_STATUS, incorporationUri + VALIDATION_STATUS_URI_SUFFIX);

        if (transactionUtils.isForRegistration(transaction)) {
            linksMap.put(LINK_COSTS, incorporationUri + COSTS_URI_SUFFIX);
        }

        incorporationResource.setLinks(linksMap);
        incorporationResource.setKind(kind);

        return incorporationResource;
    }

    public LimitedPartnershipIncorporationDto getIncorporation(Transaction transaction,
                                                               String incorporationId,
                                                               boolean includeSubResources) throws ServiceException {
        String submissionUri = getSubmissionUri(transaction.getId(), incorporationId);
        if (!transactionService.isTransactionLinkedToLimitedPartnershipIncorporation(transaction, submissionUri)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches incorporation id: %s", transaction.getId(), incorporationId));
        }

        var incorporation = repository.findById(incorporationId);
        LimitedPartnershipIncorporationDao incorporationDao = incorporation.orElseThrow(() -> new ResourceNotFoundException(String.format("Incorporation with id %s not found", incorporationId)));

        LimitedPartnershipIncorporationDto incorporationDto = mapper.daoToDto(incorporationDao);

        if (includeSubResources) {
            var subResourcesDto = new IncorporationSubResourcesDto();

            subResourcesDto.setGeneralPartners(generalPartnerService.getGeneralPartnerList(transaction));
            subResourcesDto.setLimitedPartners(limitedPartnerService.getLimitedPartnerList(transaction));
            subResourcesDto.setPartnership(limitedPartnershipService.getLimitedPartnership(transaction));

            incorporationDto.setSubResources(subResourcesDto);
        }

        return incorporationDto;
    }

    public List<ValidationStatusError> validateIncorporation(Transaction transaction)
            throws ServiceException {
        List<ValidationStatusError> errors = new ArrayList<>();

        errors.addAll(limitedPartnershipService.validateLimitedPartnership(transaction));
        errors.addAll(generalPartnerService.validateGeneralPartners(transaction));
        errors.addAll(limitedPartnerService.validateLimitedPartners(transaction));

        return errors;
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
