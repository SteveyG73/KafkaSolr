/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kafkasolr;

import java.net.URI;
import java.security.Principal;
import org.apache.http.auth.*;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.entity.*;
import org.apache.http.impl.auth.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sgar241
 */
public final class SolrConnect {
    
    private static final Logger LOG = LogManager.getRootLogger();
    private static final String SOLR_UPDATE = "/update/json/docs";
    private String solrHost;
    private int solrPort;
    private String solrCollection;
    
    private CloseableHttpClient client;
    private HttpClientContext context;

    /**
     * @return the solrHost
     */
    public String getSolrHost() {
        return solrHost;
    }

    /**
     * @param solrHost the solrHost to set
     */
    public void setSolrHost(String solrHost) {
        this.solrHost = solrHost;
    }

    /**
     * @return the solrPort
     */
    public int getSolrPort() {
        return solrPort;
    }

    /**
     * @param solrPort the solrPort to set
     */
    public void setSolrPort(int solrPort) {
        this.solrPort = solrPort;
    }

    /**
     * @return the solrCollection
     */
    public String getSolrCollection() {
        return solrCollection;
    }

    /**
     * @param solrCollection the solrCollection to set
     */
    public void setSolrCollection(String solrCollection) {
        this.solrCollection = solrCollection;
    }


    
    public SolrConnect(String host, int port, String collection) {
     
        LOG.debug("Setting Solr URI fields to >> host:"+host+", port:"+Integer.toString(port)+", collection:"+collection);
        setSolrHost(host);
        setSolrPort(port);
        solrCollection=collection;
        
        LOG.debug("Building Kerberos client context");

        Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true))
                .build();

        client = HttpClients.custom().setDefaultAuthSchemeRegistry(authSchemeRegistry).build();
        context = HttpClientContext.create();
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        // This may seem odd, but specifying 'null' as principal tells java to use the logged in user's credentials
        Credentials useJaasCreds = new Credentials() {

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public Principal getUserPrincipal() {
                return null;
            }

        };
        credentialsProvider.setCredentials( new AuthScope(null, -1, null), useJaasCreds );
        context.setCredentialsProvider(credentialsProvider);

        
    } 
    private String buildSolrCommand (){
        return null;
    }
    
    public int putJson (String jsonDoc) {
        int responseCode = 0;
        String responseBody;
        String solrPath = "/solr/"+getSolrCollection()+SOLR_UPDATE;
                  
        try {
            URI uri = new URIBuilder()
                         .setScheme("http")
                         .setHost(getSolrHost())
                         .setPort(getSolrPort())
                         .setPath(solrPath)
                         .setParameter("commit", "true")
                         .build();
            
            HttpPost post = new HttpPost(uri);
            LOG.debug(uri.toString());
            //HttpPost post = new HttpPost("http://lonvm2163.markelintl.markelgroup.com:8983/solr/IMSApplicationLogs/update/json/docs?commit=true");
            StringEntity jsonEnt = new StringEntity(jsonDoc,"UTF-8");
            jsonEnt.setContentType("application/json");
            post.addHeader("content-type", "application/json");
            post.setEntity(jsonEnt);
            LOG.debug("Sending document to Solr");
            CloseableHttpResponse response = client.execute(post,context);
            responseCode = response.getStatusLine().getStatusCode();
            LOG.debug("HTTP Response:"+responseCode);
            
            if (responseCode != 200) {
                LOG.error("Solr POST action failed with code: "+responseCode);
                responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                LOG.error(responseBody);
            }
            if (LOG.isDebugEnabled()) {
                responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                LOG.debug("HTTP body: "+responseBody);
            }
            LOG.debug("Closing the response");
            response.close();
        }
        catch (Exception e) {
            LOG.error("Solr post exception:", e);
        }
        return(responseCode);
      
    }

    public void tearDown () {
        LOG.info("Disconnecting from Solr");
        try {
            client.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
            
            
    }


    
}
