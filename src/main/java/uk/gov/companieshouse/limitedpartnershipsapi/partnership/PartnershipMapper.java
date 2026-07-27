package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipNameEnding;

@Mapper(componentModel = "spring")
public interface PartnershipMapper {

    PartnershipMapper INSTANCE = Mappers.getMapper(PartnershipMapper.class);

    PartnershipDao dtoToDao(PartnershipDto dto);

    PartnershipDto daoToDto(PartnershipDao dao);

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
