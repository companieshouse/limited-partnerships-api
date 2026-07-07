package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.NatureOfControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.NatureOfControlDto;

public class NatureOfControlBuilder {
	private NatureOfControlType type = NatureOfControlType.INDIVIDUAL;
	private boolean shareOfAssets25To50 = false;
	private boolean shareOfAssets50To75 = false;
	private boolean shareOfAssets75To100 = false;
	private boolean shareOfAssetsDoesNotApply = false;
	private boolean votingRights25To50 = false;
	private boolean votingRights50To75 = false;
	private boolean votingRights75To100 = false;
	private boolean votingRightsDoesNotApply = false;
	private boolean rightToAppointmentAndRemove = false;
	private boolean significantInfluenceControl = false;

	public NatureOfControlBuilder withType(NatureOfControlType type) {
		this.type = type;
		return this;
	}

	public NatureOfControlBuilder withShareOfAssets25To50() {
		this.shareOfAssets25To50 = true;
		return this;
	}

	public NatureOfControlBuilder withShareOfAssets50To75() {
		this.shareOfAssets50To75 = true;
		return this;
	}

	public NatureOfControlBuilder withShareOfAssets75To100() {
		this.shareOfAssets75To100 = true;
		return this;
	}

	public NatureOfControlBuilder withShareOfAssetsDoesNotApply() {
		this.shareOfAssetsDoesNotApply = true;
		return this;
	}

	public NatureOfControlBuilder withVotingRights25To50() {
		this.votingRights25To50 = true;
		return this;
	}

	public NatureOfControlBuilder withVotingRights50To75() {
		this.votingRights50To75 = true;
		return this;
	}

	public NatureOfControlBuilder withVotingRights75To100() {
		this.votingRights75To100 = true;
		return this;
	}

	public NatureOfControlBuilder withVotingRightsDoesNotApply() {
		this.votingRightsDoesNotApply = true;
		return this;
	}

	public NatureOfControlBuilder withRightToAppointmentAndRemove() {
		this.rightToAppointmentAndRemove = true;
		return this;
	}

	public NatureOfControlBuilder withSignificantInfluenceControl() {
		this.significantInfluenceControl = true;
		return this;
	}

	public NatureOfControlDto build() {
		NatureOfControlDto natureOfControlDto = new NatureOfControlDto();

		natureOfControlDto.setType(type);
		natureOfControlDto.setShareOfAssets25To50(shareOfAssets25To50);
		natureOfControlDto.setShareOfAssets50To75(shareOfAssets50To75);
		natureOfControlDto.setShareOfAssets75To100(shareOfAssets75To100);
		natureOfControlDto.setShareOfAssetsDoesNotApply(shareOfAssetsDoesNotApply);
		natureOfControlDto.setVotingRights25To50(votingRights25To50);
		natureOfControlDto.setVotingRights50To75(votingRights50To75);
		natureOfControlDto.setVotingRights75To100(votingRights75To100);
		natureOfControlDto.setVotingRightsDoesNotApply(votingRightsDoesNotApply);
		natureOfControlDto.setRightToAppointmentAndRemove(rightToAppointmentAndRemove);
		natureOfControlDto.setSignificantInfluenceControl(significantInfluenceControl);

		return natureOfControlDto;
	}
}
