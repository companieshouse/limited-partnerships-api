package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.GeneralPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;

@Service
public class GeneralPartnerService {

    private final GeneralPartnerRepository repository;
    private final GeneralPartnerMapper mapper;
    private final TransactionService transactionService;

    public GeneralPartnerService(GeneralPartnerRepository repository,
                                 GeneralPartnerMapper mapper,
                                 TransactionService transactionService) {
        this.repository = repository;
        this.mapper = mapper;
        this.transactionService = transactionService;
    }

    public String createGeneralPartner(Transaction transaction, GeneralPartnerDto generalPartnerDto, String requestId, String userId) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {

        checkNationalities(generalPartnerDto);
        GeneralPartnerDao dao = mapper.dtoToDao(generalPartnerDto);
        GeneralPartnerDao insertedSubmission = insertDaoWithMetadata(requestId, transaction, userId, dao);
        String submissionUri = linkAndSaveDao(transaction, insertedSubmission.getId(), dao);
        updateTransactionWithGeneralPartnerTransactionResourceLinks(requestId, transaction, submissionUri);

        return insertedSubmission.getId();
    }

     private GeneralPartnerDao insertDaoWithMetadata(
            String requestId, Transaction transaction, String userId, GeneralPartnerDao dao) {
        dao.getData().setKind(FILING_KIND_GENERAL_PARTNER);
        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);
        dao.setTransactionId(transaction.getId());

        GeneralPartnerDao insertedSubmission = repository.insert(dao);
        ApiLogger.infoContext(requestId, String.format("General Partner submission created with id: %s", insertedSubmission.getId()));
        return insertedSubmission;
    }

    private String linkAndSaveDao(Transaction transaction, String submissionId, GeneralPartnerDao dao) {
        var submissionUri = String.format(URL_GET_GENERAL_PARTNER, transaction.getId(), submissionId);
        dao.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        repository.save(dao);
        return submissionUri;
    }

    private void updateTransactionWithGeneralPartnerTransactionResourceLinks(
            String requestId, Transaction transaction, String submissionUri)  throws ServiceException {
        var generalPartnerResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", submissionUri);

        generalPartnerResource.setLinks(linksMap);
        generalPartnerResource.setKind(FILING_KIND_GENERAL_PARTNER);

        transaction.setResources(Collections.singletonMap(submissionUri, generalPartnerResource));

        transactionService.updateTransaction(transaction, requestId);
    }

    private void checkNationalities(GeneralPartnerDto generalPartnerDto) throws MethodArgumentNotValidException, NoSuchMethodException {
        if (!isSecondNationalityDifferent(generalPartnerDto.getData().getNationality1(), generalPartnerDto.getData().getNationality2())) {
            var methodParameter = new MethodParameter(GeneralPartnerDataDto.class.getConstructor(),-1);

            BindingResult bindingResult = new BeanPropertyBindingResult(generalPartnerDto, "GeneralPartnerDto");
            var fieldError = new FieldError("GeneralPartnerDto", "Nationality", "Second nationality must be different from the first");
            bindingResult.addError(fieldError);

            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    public boolean isSecondNationalityDifferent(Nationality nationality1, Nationality nationality2) {
        if (nationality2 == null || nationality2 == Nationality.UNKNOWN) {
            if (nationality1 == null) {
              return false;
            } else {
              return true;
            }
        }
        return nationality1 != nationality2;
    }
}
