package systems.pqp.hsdb.dao.graphql;

public class GraphQLDocument {

    private String id;
    private Integer duration;
    private GraphQLFusedBy[] fusedBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public GraphQLFusedBy[] getFusedBy() {
        return fusedBy;
    }

    public void setFusedBy(GraphQLFusedBy[] fusedBy) {
        this.fusedBy = fusedBy;
    }

    @Override
    public String toString() {
        return "GraphQLDocument{" +
                "id='" + id + '\'' +
                ", duration=" + duration +
                '}';
    }
}
