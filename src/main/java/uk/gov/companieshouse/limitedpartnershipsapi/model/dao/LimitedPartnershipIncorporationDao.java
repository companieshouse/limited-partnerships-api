package uk.gov.companieshouse.limitedpartnershipsapi.model.dao;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "limited_partnership_incorporations")
public class LimitedPartnershipIncorporationDao {
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
    private IncorporationDataDao data;

    @Field("links")
    private Map<String, String> links;

    public LimitedPartnershipIncorporationDao() {
        // used by Spring MongoDB
    }

    public LimitedPartnershipIncorporationDao(Builder builder) {
        this.createdAt = builder.createdAt;
        this.createdBy = builder.createdBy;
        this.data = builder.data;
    }

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

    public IncorporationDataDao getData() { return data; }

    public void setData(IncorporationDataDao data) {
        this.data = data;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    public static class Builder {

        private LocalDateTime createdAt;
        private String createdBy;
        private IncorporationDataDao data;

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setData(IncorporationDataDao data) {
            this.data = data;
            return this;
        }

        public LimitedPartnershipIncorporationDao build() {
            return new LimitedPartnershipIncorporationDao(this);
        }
    }
}
