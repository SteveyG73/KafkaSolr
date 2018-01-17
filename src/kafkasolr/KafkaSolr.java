/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kafkasolr;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Enumeration;
import org.apache.logging.log4j.*;

/**
 *
 * @author sgar241
 */
public class KafkaSolr {

    private static final Logger LOG = LogManager.getRootLogger();
    private static ArrayList<ListenerThread> threads;

    public static Properties getProps(String propFile) {
        Properties props = new Properties();
        InputStream input = null;

        try {
            String serviceRoot = System.getenv("SERVICE_ROOT");
            LOG.debug("SERVICE_ROOT value:"+serviceRoot);
            if (serviceRoot==null) {
                serviceRoot = ".";
            }
            LOG.info("Setting SYSTEM_ROOT to: "+serviceRoot);
            input = new FileInputStream(serviceRoot+"/config/"+propFile);
            props.load(input);
        } catch (Exception ex) {
            LOG.error("Error reading consumer properties file", ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOG.error("Error closing consumer properties file", e);
                }
            }
        }

        return (props);

    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown") {
            @Override
            public void run() {
                LOG.info("In shutdown hook - shutting down "+ Integer.toString(threads.size()) +" threads");
                for (ListenerThread list : threads) {
                    LOG.info("Stopping thread " + list.thread.getName());
                    list.listener.shutdown();
                    try {
//                        list.thread.join(5000);
                          Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        LOG.error(e);
                    }
                }
                LOG.info("Shutdown complete");
            }
        });
        
        LOG.info("Starting up Topic Listeners");

        threads = new ArrayList<>();

        boolean startFromBeginning;

        if ((args.length != 0) && ("StartFromBeginning".equals(args[0]))) {
            startFromBeginning = true;
            LOG.info("StartFromBeginning set to true.");
        } else {
            startFromBeginning = false;
            LOG.warn("StartFromBeginning not specified. Defaulting to latest position on each topic");
        }

        //Get the list of topics and collections from the properties file.
        Properties props = getProps("consumer.properties");
        Properties topics = getProps("topic_collections.properties");
        Enumeration e = topics.propertyNames();
        
        while (e.hasMoreElements()) {
            String topic = (String)e.nextElement();
            TopicListener tl = new TopicListener(props, topic, topics.getProperty(topic), startFromBeginning);
            Thread t = new Thread(tl, topic);
            threads.add(new ListenerThread(t, tl));
            //Start the thread up
            t.start();
        }

 

//        for (TopicCollection tc : topicCollections) {
//            //Create a thread for the topic and add it to the ArrayList
//            TopicListener tl = new TopicListener(props, tc.getTopicName(), tc.getSolrCollection(), startFromBeginning);
//            Thread t = new Thread(tl, tc.getTopicName());
//            threads.add(new ListenerThread(t, tl));
//            //Start the thread up
//            t.start();
//        }

        int trun;

        do {
            trun = 0;
            for (ListenerThread list : threads) {
                if (list.thread.isAlive()) {
                    ++trun;
                    LOG.info("Thread: " + list.thread.getName() + " Message count: " + Integer.toString(list.listener.getMsgCounter()));
                } else {
                    LOG.warn("Thread: " + list.thread.getName() + " has stopped");
                }
            }
            Thread.sleep(60000);
        } while (trun > 0);
        LOG.warn("No threads running - shutting down");

    }

}
