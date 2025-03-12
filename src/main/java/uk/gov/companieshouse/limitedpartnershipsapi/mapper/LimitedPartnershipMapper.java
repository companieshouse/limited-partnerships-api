package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipSubmissionDto;

@Component
@Mapper(componentModel = "spring")
public interface LimitedPartnershipMapper {

    LimitedPartnershipMapper INSTANCE = Mappers.getMapper(LimitedPartnershipMapper.class);

    LimitedPartnershipSubmissionDao dtoToDao(LimitedPartnershipSubmissionDto dto);

    LimitedPartnershipSubmissionDto daoToDto(LimitedPartnershipSubmissionDao dao);

    default String mapPartnershipNameEndingToString(PartnershipNameEnding nameEnding) {
        return nameEnding.getDescription();
    }

    default PartnershipNameEnding mapPartnershipNameEndingToEnum(String nameEnding) {
        return nameEnding != null ? PartnershipNameEnding.fromDescription(nameEnding) : null;
    }

    default String mapJurisdictionToString(Jurisdiction jurisdiction) {
        return jurisdiction.getDescription();
    }

    default Jurisdiction mapJurisdictionToEnum(String jurisdiction) {
        return jurisdiction != null ? Jurisdiction.fromDescription(jurisdiction) : null;
    }
}
