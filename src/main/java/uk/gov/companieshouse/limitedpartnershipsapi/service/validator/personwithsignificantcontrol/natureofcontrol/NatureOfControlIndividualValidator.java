package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.natureofcontrol;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.NatureOfControlDto;

@Component
public class NatureOfControlIndividualValidator {
	public boolean isValid(NatureOfControlDto natureOfControlDto) {
		if (natureOfControlDto == null) {
			return false;
		}

		if (natureOfControlDto.getType() == null) {
			return false;
		}

		if (natureOfControlDto.hasNoAssets() || natureOfControlDto.hasNoVoting()) {
			return false;
		}

		// Cannot have both rightToAppointmentAndRemovePersons and sigInfluenceControl set to true
		if (Boolean.TRUE.equals(natureOfControlDto.getRightToAppointmentAndRemove()) &&
				Boolean.TRUE.equals(natureOfControlDto.getSignificantInfluenceControl())) {
			return false;
		}

		// sigInfluenceControl=true is only valid when both surplus and voting are doesNotApply (no percent values)
		if (Boolean.TRUE.equals(natureOfControlDto.getSignificantInfluenceControl()) &&
				(natureOfControlDto.hasShareAssetsPercent() || natureOfControlDto.hasVotingRightsPercent())) {
			return false;
		}

		// If neither surplus nor voting has a percent value (both are doesNotApply),
		// at least one of rightToAppointment or sigInfluence must be true
		return natureOfControlDto.hasShareAssetsPercent() ||
				natureOfControlDto.hasVotingRightsPercent() ||
				Boolean.TRUE.equals(natureOfControlDto.getRightToAppointmentAndRemove()) ||
				Boolean.TRUE.equals(natureOfControlDto.getSignificantInfluenceControl());
	}
}
