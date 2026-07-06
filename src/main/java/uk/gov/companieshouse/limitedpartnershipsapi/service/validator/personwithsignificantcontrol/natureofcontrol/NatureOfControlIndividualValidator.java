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

		if (!hasAssets(natureOfControlDto) || !hasVoting(natureOfControlDto)) {
			return false;
		}

		// Cannot have both rightToAppointmentAndRemovePersons and sigInfluenceControl set to true
		if (Boolean.TRUE.equals(natureOfControlDto.getRightToAppointmentAndRemove()) &&
				Boolean.TRUE.equals(natureOfControlDto.getSignificantInfluenceControl())) {
			return false;
		}

		// sigInfluenceControl=true is only valid when both surplus and voting are doesNotApply (no percent values)
		if (Boolean.TRUE.equals(natureOfControlDto.getSignificantInfluenceControl()) &&
				(hasShareAssetsPercent(natureOfControlDto) || hasVotingRightsPercent(natureOfControlDto))) {
			return false;
		}

		// If neither surplus nor voting has a percent value (both are doesNotApply),
		// at least one of rightToAppointment or sigInfluence must be true
		if (!hasShareAssetsPercent(natureOfControlDto) &&
				!hasVotingRightsPercent(natureOfControlDto) &&
				!Boolean.TRUE.equals(natureOfControlDto.getRightToAppointmentAndRemove()) &&
				!Boolean.TRUE.equals(natureOfControlDto.getSignificantInfluenceControl())) {
			return false;
		}

		return true;
	}

	private boolean hasShareAssetsPercent(NatureOfControlDto natureOfControlDto) {
		return Boolean.TRUE.equals(natureOfControlDto.getShareOfAssets25To50()) ||
				Boolean.TRUE.equals(natureOfControlDto.getShareOfAssets50To75()) ||
				Boolean.TRUE.equals(natureOfControlDto.getShareOfAssets75To100());
	}

	private boolean hasVotingRightsPercent(NatureOfControlDto natureOfControlDto) {
		return Boolean.TRUE.equals(natureOfControlDto.getVotingRights25To50()) ||
				Boolean.TRUE.equals(natureOfControlDto.getVotingRights50To75()) ||
				Boolean.TRUE.equals(natureOfControlDto.getVotingRights75To100());
	}

	// Must have a surplus assets field set (either a percentage or doesNotApply)
	private boolean hasAssets(NatureOfControlDto natureOfControlDto) {
		return hasShareAssetsPercent(natureOfControlDto) ||
				Boolean.TRUE.equals(natureOfControlDto.getShareOfAssetsDoesNotApply());
	}

	// Must have a voting rights field set (either a percentage or doesNotApply)
	private boolean hasVoting(NatureOfControlDto natureOfControlDto) {
		return hasVotingRightsPercent(natureOfControlDto) ||
				Boolean.TRUE.equals(natureOfControlDto.getVotingRightsDoesNotApply());
	}
}
