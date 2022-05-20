package systems.pqp.hsdb.dao.graphql;

import java.util.Arrays;

public class GraphQLNode {

    String id;
    String coreId;
    String[] externalIds;
    String title;
    String url;
    String sharingUrl;
    GraphQLDocument coreDocument;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[]  getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(String[]  externalIds) {
        this.externalIds = externalIds;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSharingUrl() {
        return sharingUrl;
    }

    public void setSharingUrl(String sharingUrl) {
        this.sharingUrl = sharingUrl;
    }

    public GraphQLDocument getGraphQLDocument() {
        return coreDocument;
    }

    public void setGraphQLDocument(GraphQLDocument coreDocument) {
        this.coreDocument = coreDocument;
    }

    public String getEpisodeId(){
        String episodePattern = ":episode:";
        if(null != getExternalIds()) {
            for (String _id : getExternalIds()) {
                if (_id.contains(episodePattern)) {
                    return _id;
                }
            }
        }
        if( null != getGraphQLDocument() && null != getGraphQLDocument().getFusedBy() ) {
            for (GraphQLFusedBy fusedBy : getGraphQLDocument().getFusedBy()) {
                if (fusedBy.getId().contains(":episode")) {
                    return fusedBy.getId();
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "GraphQLItem{" +
                "id='" + id + '\'' +
                ", coreId='" + coreId + '\'' +
                ", externalIds=" + Arrays.toString(externalIds) +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", sharingUrl='" + sharingUrl + '\'' +
                ", coreDocument=" + coreDocument +
                '}';
    }
}
