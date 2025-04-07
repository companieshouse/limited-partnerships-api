package uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;

import java.util.List;

public class DataDao {

    @Field("partnership_name")
    private String partnershipName;

    @Field("name_ending")
    private String nameEnding;

    @Field("registered_email_address")
    private String email;

    @Field("partnership_type")
    private PartnershipType partnershipType;

    @Field("jurisdiction")
    private String jurisdiction;

    @Field("registered_office_address")
    private AddressDao registeredOfficeAddress;

    @Field("term")
    private Term term;

    @Field("principal_place_of_business_address")
    private AddressDao principalPlaceOfBusinessAddress;

    @Field("sic_codes")
    private List<String> sicCodes;

    public String getPartnershipName() {
        return partnershipName;
    }

    public void setPartnershipName(String partnershipName) {
        this.partnershipName = partnershipName;
    }

    public String getNameEnding() {
        return nameEnding;
    }

    public void setNameEnding(String nameEnding) {
        this.nameEnding = nameEnding;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PartnershipType getPartnershipType() {
        return partnershipType;
    }

    public void setPartnershipType(PartnershipType partnershipType) {
        this.partnershipType = partnershipType;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public AddressDao getRegisteredOfficeAddress() {
        return registeredOfficeAddress;
    }

    public void setRegisteredOfficeAddress(AddressDao registeredOfficeAddress) {
        this.registeredOfficeAddress = registeredOfficeAddress;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public AddressDao getPrincipalPlaceOfBusinessAddress() {
        return principalPlaceOfBusinessAddress;
    }

    public void setPrincipalPlaceOfBusinessAddress(AddressDao principalPlaceOfBusinessAddress) {
        this.principalPlaceOfBusinessAddress = principalPlaceOfBusinessAddress;
    }

    public List<String> getSicCodes() {
        return sicCodes;
    }

    public void setSicCodes(List<String> sicCodes) {
        this.sicCodes = sicCodes;
    }
}
