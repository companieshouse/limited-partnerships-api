package uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao;

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

    public IncorporationDataDao getData() {
        if (data == null) {
            data = new IncorporationDataDao();
        }
        return data;
    }

    public void setData(IncorporationDataDao data) {
        this.data = data;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }
}
