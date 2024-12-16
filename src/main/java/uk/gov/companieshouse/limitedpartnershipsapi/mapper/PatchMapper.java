package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.PatchDto;

@Mapper(uses = JsonNullableMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface PatchMapper {

    DataDto map(PatchDto entity);

    PatchDto map(DataDto entity);

    @InheritConfiguration
    void update(PatchDto update, @MappingTarget DataDto destination);
}
