package uk.gov.companieshouse.limitedpartnershipsapi;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Utility class to manage Testcontainers and their versions.
 */
public class Containers {

    private Containers() {}

    public static final String MONGO_IMAGE = "mongo:8.0.11-noble";

    public static MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer(DockerImageName.parse(MONGO_IMAGE));
    }
}
