package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.natureofcontrol;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.NatureOfControlDto;

@Component
public class NatureOfControlFirmValidator extends AbstractNatureOfControlValidator {
    public boolean isValid(NatureOfControlDto natureOfControlDto) {
        if (natureOfControlDto == null) {
            return false;
        }

        if (natureOfControlDto.getType() == null) {
            return false;
        }

        if (hasNoAssets(natureOfControlDto) || hasNoVoting(natureOfControlDto)) {
            return false;
        }


        // Valid if at least one nature of control is selected
        // either share assets percent, share voting rights percent or right to appointment or significant influence control
        return hasShareAssetsPercent(natureOfControlDto) ||
                hasVotingRightsPercent(natureOfControlDto) ||
                Boolean.TRUE.equals(natureOfControlDto.getRightToAppointmentAndRemove()) ||
                Boolean.TRUE.equals(natureOfControlDto.getSignificantInfluenceControl());
    }
}
