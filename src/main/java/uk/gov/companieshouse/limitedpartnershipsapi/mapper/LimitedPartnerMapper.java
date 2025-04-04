package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.LimitedPartnerType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;

@Component
@Mapper(componentModel = "spring")
public interface LimitedPartnerMapper {

    LimitedPartnerMapper INSTANCE = Mappers.getMapper(LimitedPartnerMapper.class);

    LimitedPartnerDto daoToDto(LimitedPartnerDao dao);

    LimitedPartnerDao dtoToDao(LimitedPartnerDto dto);

    default String mapPartnerTypeToString(LimitedPartnerType partnerType) {
        return partnerType.getDescription();
    }

    default LimitedPartnerType mapPartnerTypeToEnum(String partnerType) {
        return partnerType != null ? LimitedPartnerType.fromDescription(partnerType) : null;
    }
}
