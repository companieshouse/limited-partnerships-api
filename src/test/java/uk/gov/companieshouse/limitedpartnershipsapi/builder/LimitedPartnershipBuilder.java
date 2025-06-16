package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import static uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType.PFLP;

public class LimitedPartnershipBuilder {
    public static final String SUBMISSION_ID = "abc-123";

    public LimitedPartnershipDto dto() {
        LimitedPartnershipDto dto = new LimitedPartnershipDto();

        DataDto dataDao = new DataDto();
        dataDao.setPartnershipName("test name");
        dataDao.setNameEnding(PartnershipNameEnding.L_DOT_P_DOT);
        dataDao.setPartnershipType(PartnershipType.LP);
        dto.setData(dataDao);

        return dto;
    }

    public LimitedPartnershipDao dao() {
        LimitedPartnershipDao dao = new LimitedPartnershipDao();

        dao.setId(SUBMISSION_ID);
        DataDao dataDao = new DataDao();
        dataDao.setPartnershipType(PFLP);
        dataDao.setPartnershipName("Asset Adders");
        dataDao.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP.getDescription());
        dataDao.setEmail("some@where.com");
        dataDao.setJurisdiction(Jurisdiction.ENGLAND_AND_WALES.getApiKey());
        dataDao.setRegisteredOfficeAddress(createAddressDao());
        dataDao.setPrincipalPlaceOfBusinessAddress(createAddressDao());
        dataDao.setLawfulPurposeStatementChecked(true);
        dao.setData(dataDao);

        return dao;
    }

    private AddressDao createAddressDao() {
        AddressDao dao = new AddressDao();

        dao.setPremises("33");
        dao.setAddressLine1("Acacia Avenue");
        dao.setLocality("Birmingham");
        dao.setCountry("England");
        dao.setPostalCode("BM1 2EH");

        return dao;
    }
}
