package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.partnership;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.PostTransitionStrategy;

import java.util.List;

@Component
public class RedesignateToPFLP implements PostTransitionStrategy<LimitedPartnershipDto> {

    @Value("${REDESIGNATE_TO_PFLP_COST}")
    private String redesignateToPflpCost;

    private static final String COST_DESCRIPTION = "Redesignate to pflp fee";

    @Override
    public String getKind() {
        return PartnershipKind.UPDATE_PARTNERSHIP_REDESIGNATE_TO_PFLP.getDescription();
    }

    @Override
    public void validate(LimitedPartnershipDto limitedPartnershipDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus, Transaction transaction) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {

        if((limitedPartnershipDto.getData().getPartnershipType() == PartnershipType.PFLP ||
           limitedPartnershipDto.getData().getPartnershipType() == PartnershipType.SPFLP) &&
           limitedPartnershipDto.getData().getRedesignateToPFLP() != null) {
           errorsList.add(validationStatus.createValidationStatusError("Redesignate to pflp is not required",
                   "data.redesignateToPFLP"));
        }
    }

    @Override
    public Cost getCost(Cost cost) {
        String productType = "lp-" + getKind().split("#")[1];

        cost.setAmount(redesignateToPflpCost);
        cost.setDescription(COST_DESCRIPTION);
        cost.setProductType(productType);

        return cost;
    }
}
