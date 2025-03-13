package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.LimitedPartnershipIncorporationDto;

@Component
@Mapper(componentModel = "spring")
public interface LimitedPartnershipIncorporationMapper {

    LimitedPartnershipIncorporationMapper INSTANCE = Mappers.getMapper(LimitedPartnershipIncorporationMapper.class);

    @Mapping(target = "kind", source = "dao.data.kind")
    @Mapping(target = "etag", source = "dao.data.etag")
    LimitedPartnershipIncorporationDto daoToDto(LimitedPartnershipIncorporationDao dao);
}
