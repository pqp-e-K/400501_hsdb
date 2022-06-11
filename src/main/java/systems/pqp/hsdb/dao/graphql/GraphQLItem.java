package systems.pqp.hsdb.dao.graphql;

import java.util.Arrays;

public class GraphQLItem {

    private GraphQLEdge[] edges;

    public GraphQLEdge[] getEdges() {
        return edges;
    }

    public void setEdges(GraphQLEdge[] edges) {
        this.edges = edges;
    }

    @Override
    public String toString() {
        return "GraphQLItem{" +
                "edges=" + Arrays.toString(edges) +
                '}';
    }
}
