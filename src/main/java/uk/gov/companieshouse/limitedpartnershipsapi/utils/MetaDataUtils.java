package uk.gov.companieshouse.limitedpartnershipsapi.utils;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.BaseDao;

public class MetaDataUtils {

    private MetaDataUtils() {
        // Private constructor to prevent instantiation
    }

    public static void copyMetaDataForPatch(BaseDao daoBeforePatch,
                                            BaseDao daoAfterPatch) {
        daoAfterPatch.setId(daoBeforePatch.getId());
        daoAfterPatch.setCreatedAt(daoBeforePatch.getCreatedAt());
        daoAfterPatch.setCreatedBy(daoBeforePatch.getCreatedBy());
        daoAfterPatch.setLinks(daoBeforePatch.getLinks());
        daoAfterPatch.setTransactionId(daoBeforePatch.getTransactionId());
    }

    public static void setAuditDetailsForPatch(BaseDao daoAfterPatch, String userId) {
        daoAfterPatch.setUpdatedBy(userId);
    }
}
