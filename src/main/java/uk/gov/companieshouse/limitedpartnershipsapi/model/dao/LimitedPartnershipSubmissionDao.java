package uk.gov.companieshouse.limitedpartnershipsapi.model.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "limited_partnership_submissions")
public class LimitedPartnershipSubmissionDao {

    @Id
    private String id;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("data")
    private DataDao data;

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

    public DataDao getData() {
        return data;
    }

    public void setData(DataDao data) {
        this.data = data;
    }
}
