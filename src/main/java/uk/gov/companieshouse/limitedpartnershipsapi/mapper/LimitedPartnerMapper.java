package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnerDto;

@Component
@Mapper(componentModel = "spring")
public interface LimitedPartnerMapper {

    LimitedPartnerMapper INSTANCE = Mappers.getMapper(LimitedPartnerMapper.class);

    @Mapping(target = "kind", source = "dao.data.kind")
    @Mapping(target = "etag", source = "dao.data.etag")
    LimitedPartnerDto daoToDto(LimitedPartnerDao dao);
}
