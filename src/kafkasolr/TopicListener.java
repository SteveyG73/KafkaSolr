/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kafkasolr;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sgar241
 */
public class TopicListener implements Runnable {

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private static final Logger LOG = LogManager.getRootLogger();
    private final Properties props;
    private final String topicName;
    private final boolean startFromBeginning;
    private final String solrCollection;
    private final KafkaConsumer<String, String> consumer;

    private int msgCounter;

    public TopicListener(Properties kafkaProps, String kafkaTopicName, String solrCollectionName, boolean kafkaStartBeginning) {
        LOG.info("Creating Kafka consumer for topic "+kafkaTopicName);
        props = kafkaProps;
        topicName = kafkaTopicName;
        solrCollection = solrCollectionName;
        startFromBeginning = kafkaStartBeginning;
        msgCounter = 0;
        consumer = new KafkaConsumer<>(props);
    }

    @Override
    public void run() {

        try {
            
            LOG.debug("Subscribing to topic <" + topicName + ">");
            if (startFromBeginning) {
                LOG.debug("Set to begining of topic");
                TopicPartition part0 = new TopicPartition(topicName, 0);
                consumer.assign(Arrays.asList(part0));
                consumer.seekToBeginning(part0);
                LOG.debug("Next record: " + consumer.position(part0));
            } else {
//                consumer.subscribe(Arrays.asList(topicName));
                LOG.debug("Set to end of topic");
                TopicPartition part0 = new TopicPartition(topicName, 0);
                consumer.assign(Arrays.asList(part0));
                consumer.seekToEnd(part0);
                LOG.debug("Next record: " + consumer.position(part0));
            }

            SolrConnect solr = new SolrConnect("lonvm2163.markelintl.markelgroup.com", 8983, solrCollection);
            LOG.info("Ready to read messages from topic "+topicName);
            try {

                while (!closed.get()) {
                    ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                    for (ConsumerRecord<String, String> record : records) {
                        LOG.debug(String.format("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value()));
                        int response = solr.putJson(record.value());
                        if (response == 200) {
                            ++msgCounter;
                        }
                    }
                    //consumer.commitSync();
                }
                LOG.info("Thread has been shutdown.");
            } catch (WakeupException we) {
             // Ignore exception if closing
                if (!closed.get()) throw we;
            } finally {
                solr.tearDown();
                consumer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e);
        } finally {
            LOG.info("Thread exiting");
        }

        
        
    }

    /**
     * @return the msgCounter
     */
    public int getMsgCounter() {
        return msgCounter;
    }
    
    public void shutdown() {
        closed.set(true);
        consumer.wakeup();
    }

}
