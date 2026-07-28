package uk.gov.companieshouse.limitedpartnershipsapi.incorporation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.limitedpartnershipsapi.incorporation.dao.IncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.incorporation.dto.LimitedPartnershipIncorporationDto;

@Mapper(componentModel = "spring")
public interface IncorporationMapper {

    IncorporationMapper INSTANCE = Mappers.getMapper(IncorporationMapper.class);

    @Mapping(target = "kind", source = "dao.data.kind")
    @Mapping(target = "etag", source = "dao.data.etag")
    LimitedPartnershipIncorporationDto daoToDto(IncorporationDao dao);
}
