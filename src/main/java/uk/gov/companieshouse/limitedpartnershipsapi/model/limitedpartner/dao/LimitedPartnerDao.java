package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "limited_partners")
public class LimitedPartnerDao {
    @Id
    private String id;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("created_by_user_id")
    private String createdBy;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("updated_by_user_id")
    private String updatedBy;

    @Field("data")
    private LimitedPartnerDataDao data;

    @Field("links")
    private Map<String, String> links;

    @Field("transaction_id")
    private String transactionId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LimitedPartnerDataDao getData() {
        if (data == null) {
            data = new LimitedPartnerDataDao();
        }
        return data;
    }

    public void setData(LimitedPartnerDataDao data) {
        this.data = data;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
