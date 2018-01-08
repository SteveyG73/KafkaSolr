# KafkaSolr Interface

Simple interface between Kafka message broker and Solr search engine.

## How to configure

### Topics to Collections mapping

Enter a list of key-value pairs in the `config/topic_collections.properties` file:

e.g.

```
RSSNews=News
```

### Kafka Consumer 
You'll also need to complete the Kafka consumer properties file. There is an example in the `/config/` directory.

The service will start a thread for each Kafka topic you specify.

### JAAS config file
As Kafka and Solr are both using Kerberos to authenticate, you'll need a JAAS config file. I've provided two examples, one that expects you to do a `kinit` before you run the service, and another that expects a keytab file. These are also in the `/config/` directory.

## How to start it

Assuming you've managed to create the jar file from the rubbish code I've provided you can run the service using:

```bash
java -Djava.security.auth.login.config=$SERVICE_ROOT/config/kafkasolr-jaas.conf \
 -Djava.security.krb5.conf=/etc/krb5.conf \
 -Djavax.security.auth.useSubjectCredsOnly=false \
 -Dlog4j.configurationFile=$SERVICE_ROOT/config/log4j2-config.xml \
 -cp "$SERVICE_ROOT/KafkaSolr.jar:$SERVICE_ROOT/lib/*:$SERVICE_ROOT/config/*" kafkasolr.KafkaSolr
```

where `$SERVICE_ROOT` is the place you've installed your files. The JAR file is supposed to live in the top level of this directory and generally gets called `KafkaSolr.jar`.
