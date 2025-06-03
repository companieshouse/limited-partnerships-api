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
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.VALIDATION_STATUS_URI_SUFFIX;

@Service
public class LimitedPartnershipIncorporationService {

    private final LimitedPartnershipService limitedPartnershipService;

    private final LimitedPartnershipIncorporationRepository repository;
    private final TransactionService transactionService;
    private GeneralPartnerService generalPartnerService;
    private LimitedPartnerService limitedPartnerService;

    private final LimitedPartnershipIncorporationMapper mapper;

    private final TransactionUtils transactionUtils;

    public LimitedPartnershipIncorporationService(
            LimitedPartnershipService limitedPartnershipService,
            LimitedPartnershipIncorporationRepository repository,
            LimitedPartnershipIncorporationMapper mapper,
            TransactionUtils transactionUtils,
            TransactionService transactionService,
            GeneralPartnerService generalPartnerService,
            LimitedPartnerService limitedPartnerService) {
        this.limitedPartnershipService = limitedPartnershipService;
        this.repository = repository;
        this.mapper = mapper;
        this.transactionUtils = transactionUtils;
        this.transactionService = transactionService;
        this.generalPartnerService = generalPartnerService;
        this.limitedPartnerService = limitedPartnerService;
    }

    public String createIncorporation(Transaction transaction, IncorporationDto incorporationDto, String requestId, String userId)
            throws ServiceException {
        final var kind = incorporationDto.getData().getKind();

        var dao = new LimitedPartnershipIncorporationDao();
        dao.getData().setKind(kind);
        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedAt(LocalDateTime.now());
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
        var incorporationTransactionResource = createIncorporationTransactionResource(incorporationUri, kind);

        transaction.setResources(Collections.singletonMap(incorporationUri, incorporationTransactionResource));
        transactionService.updateTransaction(transaction, loggingContext);
    }

    private Resource createIncorporationTransactionResource(String incorporationUri, String kind) {
        var incorporationResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", incorporationUri);
        linksMap.put("validation_status", incorporationUri + VALIDATION_STATUS_URI_SUFFIX);
        linksMap.put("costs", incorporationUri + "/costs");

        incorporationResource.setLinks(linksMap);
        incorporationResource.setKind(kind);

        return incorporationResource;
    }

    public LimitedPartnershipIncorporationDto getIncorporation(Transaction transaction,
                                                               String incorporationId,
                                                               boolean includeSubResources) throws ServiceException {
        String submissionUri = getSubmissionUri(transaction.getId(), incorporationId);
        if (!transactionUtils.isTransactionLinkedToLimitedPartnershipIncorporation(transaction, submissionUri)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches incorporation id: %s", transaction.getId(), incorporationId));
        }

        var incorporation = repository.findById(incorporationId);
        LimitedPartnershipIncorporationDao incorporationDao = incorporation.orElseThrow(() -> new ResourceNotFoundException(String.format("Incorporation with id %s not found", incorporationId)));

        LimitedPartnershipIncorporationDto incorporationDto = mapper.daoToDto(incorporationDao);

        if (includeSubResources) {
            var subResourcesDto = new IncorporationSubResourcesDto();

            // TODO Set collections of actual General Partners and Limited Partners once implemented
            subResourcesDto.setGeneralPartners(new ArrayList<>());
            subResourcesDto.setLimitedPartners(new ArrayList<>());

            LimitedPartnershipDto partnershipDto = limitedPartnershipService.getLimitedPartnership(transaction);
            subResourcesDto.setPartnership(partnershipDto);

            incorporationDto.setSubResources(subResourcesDto);
        }

        return incorporationDto;
    }

    public List<ValidationStatusError> validateIncorporation(Transaction transaction, String incorporationId)
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
