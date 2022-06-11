package systems.pqp.hsdb.dao.graphql;

public class GraphQLPublisherGrouping {

    private String id;
    private String organizationName;
    private String coreId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getCoreId() {
        return coreId;
    }

    public void setCoreId(String coreId) {
        this.coreId = coreId;
    }

    @Override
    public String toString() {
        return "GraphQLShow{" +
                "id='" + id + '\'' +
                ", organizationName='" + organizationName + '\'' +
                ", coreId='" + coreId +
                '}';
    }
}
