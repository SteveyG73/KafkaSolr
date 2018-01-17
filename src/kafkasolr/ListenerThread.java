/*
 * (C) Markel International Services Ltd
 * All rights reserved.
 */
package kafkasolr;

/**
 *
 * @author sgar241
 */
public class ListenerThread {
    
    public Thread thread;
    
    public TopicListener listener;

    public ListenerThread(Thread thread, TopicListener listener) {
        this.thread = thread;
        this.listener = listener;
    }

    public ListenerThread() {
    }
    
    
    
}
