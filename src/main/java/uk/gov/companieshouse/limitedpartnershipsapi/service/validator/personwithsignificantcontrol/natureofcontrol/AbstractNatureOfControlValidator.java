package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.natureofcontrol;

import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.NatureOfControlDto;

abstract class AbstractNatureOfControlValidator {
    protected boolean hasShareAssetsPercent(NatureOfControlDto natureOfControlDto) {
        return Boolean.TRUE.equals(natureOfControlDto.getShareOfAssets25To50()) ||
                Boolean.TRUE.equals(natureOfControlDto.getShareOfAssets50To75()) ||
                Boolean.TRUE.equals(natureOfControlDto.getShareOfAssets75To100());
    }

    protected boolean hasVotingRightsPercent(NatureOfControlDto natureOfControlDto) {
        return Boolean.TRUE.equals(natureOfControlDto.getVotingRights25To50()) ||
                Boolean.TRUE.equals(natureOfControlDto.getVotingRights50To75()) ||
                Boolean.TRUE.equals(natureOfControlDto.getVotingRights75To100());
    }

    // Must have a surplus assets field set (either a percentage or doesNotApply)
    protected boolean hasNoAssets(NatureOfControlDto natureOfControlDto) {
        return !hasShareAssetsPercent(natureOfControlDto) &&
                !Boolean.TRUE.equals(natureOfControlDto.getShareOfAssetsDoesNotApply());
    }

    // Must have a voting rights field set (either a percentage or doesNotApply)
    protected boolean hasNoVoting(NatureOfControlDto natureOfControlDto) {
        return !hasVotingRightsPercent(natureOfControlDto) &&
                !Boolean.TRUE.equals(natureOfControlDto.getVotingRightsDoesNotApply());
    }
}
