/*
 * (C) Markel International Services Ltd
 * All rights reserved.
 */
package kafkasolr;

/**
 *
 * @author sgar241
 */
public class TopicCollection {

    
    private String topicName;
    private String solrCollection;

    public TopicCollection(String topicName, String solrCollection) {
        this.topicName = topicName;
        this.solrCollection = solrCollection;
    }

    /**
     * Get the value of topicName
     *
     * @return the value of topicName
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Set the value of topicName
     *
     * @param topicName new value of topicName
     */
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }


    /**
     * Get the value of solrCollection
     *
     * @return the value of solrCollection
     */
    public String getSolrCollection() {
        return solrCollection;
    }

    /**
     * Set the value of solrCollection
     *
     * @param solrCollection new value of solrCollection
     */
    public void setSolrCollection(String solrCollection) {
        this.solrCollection = solrCollection;
    }

}
