package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.partner;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.LimitedPartnerValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.PostTransitionStrategy;

import java.util.List;

@Component
public class RemoveLimitedPartnerLegalEntity implements PostTransitionStrategy<PartnerDto> {

    private final LimitedPartnerValidator limitedPartnerValidator;
    private final RemoveUpdatePartner removeUpdatePartner;

    RemoveLimitedPartnerLegalEntity(LimitedPartnerValidator limitedPartnerValidator, RemoveUpdatePartner removeUpdatePartner) {
        this.limitedPartnerValidator = limitedPartnerValidator;
        this.removeUpdatePartner = removeUpdatePartner;
    }

    @Override
    public String getKind() {
        return PartnerKind.REMOVE_LIMITED_PARTNER_LEGAL_ENTITY.getDescription();
    }

    @Override
    public void validate(PartnerDto partnerDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus, Transaction transaction) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        List<ValidationStatusError> errorsListValidator = limitedPartnerValidator.validateFull((LimitedPartnerDto) partnerDto, transaction);

        errorsList.addAll(errorsListValidator);

        removeUpdatePartner.validateRemove(partnerDto, errorsList, validationStatus);
    }

    @Override
    public Cost getCost(Cost cost) {
        return null;
    }
}
