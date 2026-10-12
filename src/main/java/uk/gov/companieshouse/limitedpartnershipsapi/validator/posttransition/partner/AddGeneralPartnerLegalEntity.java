package uk.gov.companieshouse.limitedpartnershipsapi.validator.posttransition.partner;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.generalpartner.GeneralPartnerValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.dto.PartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.ValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.posttransition.PostTransitionStrategy;

import java.util.List;

@Component
public class AddGeneralPartnerLegalEntity implements PostTransitionStrategy<PartnerDto> {

    private final GeneralPartnerValidationStatus generalPartnerValidationStatus;

    public AddGeneralPartnerLegalEntity(GeneralPartnerValidationStatus generalPartnerValidationStatus) {
        this.generalPartnerValidationStatus = generalPartnerValidationStatus;
    }

    @Override
    public String getKind() {
        return PartnerKind.ADD_GENERAL_PARTNER_LEGAL_ENTITY.getDescription();
    }

    @Override
    public void validate(PartnerDto partnerDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus, Transaction transaction) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        List<ValidationStatusError> errorsListValidator = generalPartnerValidationStatus.validate((GeneralPartnerDto) partnerDto, transaction, false);

        errorsList.addAll(errorsListValidator);
    }

    @Override
    public Cost getCost(Cost cost) {
        return null;
    }
}
