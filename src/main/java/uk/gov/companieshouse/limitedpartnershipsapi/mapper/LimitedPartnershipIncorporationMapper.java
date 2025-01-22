package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipIncorporationDto;

@Component
@Mapper(componentModel = "spring")
public interface LimitedPartnershipIncorporationMapper {

    LimitedPartnershipIncorporationMapper INSTANCE = Mappers.getMapper(LimitedPartnershipIncorporationMapper.class);

    // TODO Map etag
    @Mapping(target = "kind", source = "dao.data.kind")
    LimitedPartnershipIncorporationDto daoToDto(LimitedPartnershipIncorporationDao dao);
}
