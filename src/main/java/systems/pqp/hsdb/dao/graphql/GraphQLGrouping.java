package systems.pqp.hsdb.dao.graphql;

public class GraphQLGrouping {

    private String id;
    private String title;
    private String coreId;
    private GraphQLItem items;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoreId() {
        return coreId;
    }

    public void setCoreId(String coreId) {
        this.coreId = coreId;
    }

    public GraphQLItem getItems() {
        return items;
    }

    public void setItems(GraphQLItem items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "GraphQLShow{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", coreId='" + coreId + '\'' +
                ", episodes=" + items +
                '}';
    }
}
