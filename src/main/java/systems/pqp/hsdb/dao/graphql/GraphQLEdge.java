package systems.pqp.hsdb.dao.graphql;

public class GraphQLEdge {

    private GraphQLNode node;

    public GraphQLNode getNode() {
        return node;
    }

    public void setNode(GraphQLNode node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "GraphQLEdge{" +
                "node=" + node +
                '}';
    }
}
