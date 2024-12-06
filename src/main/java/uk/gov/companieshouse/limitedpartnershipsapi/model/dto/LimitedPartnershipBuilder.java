package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.DataType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;

import java.util.Map;

public class LimitedPartnershipBuilder {
    private final LimitedPartnershipMapper mapper;

//    @JsonInclude(NON_NULL)
//    @JsonProperty("data")
//    private DataDto data;

    private LimitedPartnershipSubmissionDao limitedPartnershipSubmissionDao;

    public LimitedPartnershipBuilder(LimitedPartnershipMapper mapper, LimitedPartnershipSubmissionDao limitedPartnershipSubmissionDao) {
        this.mapper = mapper;
        //        this.data = limitedPartnershipSubmissionDao.getData();
        this.limitedPartnershipSubmissionDao = limitedPartnershipSubmissionDao;
    }

    public void withData(DataType type, Map<String, Object> data) {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.convertValue(data, new TypeReference<>() {
//        });
//
//        System.out.println("updateLimitedPartnership" + data);
//        System.out.println("updateLimitedPartnership" + mapper);

        DataDao dataDao = limitedPartnershipSubmissionDao.getData();

        String email = (String) data.get("email");

        dataDao.setEmail(email);
    }

    public LimitedPartnershipSubmissionDao getDao() {
        return limitedPartnershipSubmissionDao;
    }

    public LimitedPartnershipSubmissionDto getDto() {
        return mapper.daoToDto(limitedPartnershipSubmissionDao);
    }
}
