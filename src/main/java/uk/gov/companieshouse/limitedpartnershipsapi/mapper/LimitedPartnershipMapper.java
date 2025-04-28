package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

@Component
@Mapper(componentModel = "spring")
public interface LimitedPartnershipMapper {

    LimitedPartnershipMapper INSTANCE = Mappers.getMapper(LimitedPartnershipMapper.class);

    LimitedPartnershipDao dtoToDao(LimitedPartnershipDto dto);

    LimitedPartnershipDto daoToDto(LimitedPartnershipDao dao);

    default String mapPartnershipNameEndingToString(PartnershipNameEnding nameEnding) {
        return nameEnding.getDescription();
    }

    default PartnershipNameEnding mapPartnershipNameEndingToEnum(String nameEnding) {
        return nameEnding != null ? PartnershipNameEnding.fromDescription(nameEnding) : null;
    }

    default String mapJurisdictionToString(Jurisdiction jurisdiction) {
        return jurisdiction.getApiKey();
    }

    default Jurisdiction mapJurisdictionToEnum(String jurisdiction) {
        return jurisdiction != null ? Jurisdiction.fromApiKey(jurisdiction) : null;
    }
}
