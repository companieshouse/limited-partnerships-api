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

		if (!isHasSurplus(natureOfControlDto) || !isHasVoting(natureOfControlDto)) {
			return false;
		}

		// Cannot have both rightToAppointmentAndRemovePersons and sigInfluenceControl set to true
		if (Boolean.TRUE.equals(natureOfControlDto.getRightToAppointmentAndRemovePersons()) &&
				Boolean.TRUE.equals(natureOfControlDto.getSigInfluenceControl())) {
			return false;
		}

		// sigInfluenceControl=true is only valid when both surplus and voting are doesNotApply (no percent values)
		if (Boolean.TRUE.equals(natureOfControlDto.getSigInfluenceControl()) &&
				(hasPartRightToShareSurplusAssetsPercent(natureOfControlDto) || hasVotingRightsPercent(natureOfControlDto))) {
			return false;
		}

		// If neither surplus nor voting has a percent value (both are doesNotApply),
		// at least one of rightToAppointment or sigInfluence must be true
		if (!hasPartRightToShareSurplusAssetsPercent(natureOfControlDto) &&
				!hasVotingRightsPercent(natureOfControlDto) &&
				!Boolean.TRUE.equals(natureOfControlDto.getRightToAppointmentAndRemovePersons()) &&
				!Boolean.TRUE.equals(natureOfControlDto.getSigInfluenceControl())) {
			return false;
		}

		return true;
	}

	private boolean hasPartRightToShareSurplusAssetsPercent(NatureOfControlDto natureOfControlDto) {
		return Boolean.TRUE.equals(natureOfControlDto.getPartRightToShareSurplusAssets25To50Percent()) ||
				Boolean.TRUE.equals(natureOfControlDto.getPartRightToShareSurplusAssets50To75Percent()) ||
				Boolean.TRUE.equals(natureOfControlDto.getPartRightToShareSurplusAssets75To100Percent());
	}

	private boolean hasVotingRightsPercent(NatureOfControlDto natureOfControlDto) {
		return Boolean.TRUE.equals(natureOfControlDto.getVotingRights25To50Percent()) ||
				Boolean.TRUE.equals(natureOfControlDto.getVotingRights50To75Percent()) ||
				Boolean.TRUE.equals(natureOfControlDto.getVotingRights75To100Percent());
	}

	// Must have a voting rights field set (either a percentage or doesNotApply)
	private boolean isHasVoting(NatureOfControlDto natureOfControlDto) {
		boolean hasVoting = hasVotingRightsPercent(natureOfControlDto) ||
				Boolean.TRUE.equals(natureOfControlDto.getVotingRightsDoesNotApply());
		return hasVoting;
	}

	// Must have a surplus assets field set (either a percentage or doesNotApply)
	private boolean isHasSurplus(NatureOfControlDto natureOfControlDto) {
		boolean hasSurplus = hasPartRightToShareSurplusAssetsPercent(natureOfControlDto) ||
				Boolean.TRUE.equals(natureOfControlDto.getPartRightToShareSurplusAssetsDoesNotApply());
		return hasSurplus;
	}
}
