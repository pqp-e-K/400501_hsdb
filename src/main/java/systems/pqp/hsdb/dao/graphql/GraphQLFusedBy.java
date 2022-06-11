package systems.pqp.hsdb.dao.graphql;

public class GraphQLFusedBy {

    private String id;
    private String externalId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @Override
    public String toString() {
        return "GraphQLFusedBy{" +
                "id='" + id + '\'' +
                ", externalId='" + externalId + '\'' +
                '}';
    }
}
