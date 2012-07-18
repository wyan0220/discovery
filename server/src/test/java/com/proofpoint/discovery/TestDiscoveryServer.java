/*
 * Copyright 2010 Proofpoint, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.proofpoint.discovery;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.proofpoint.configuration.ConfigurationFactory;
import com.proofpoint.configuration.ConfigurationModule;
import com.proofpoint.discovery.client.DiscoveryAnnouncementClient;
import com.proofpoint.discovery.client.DiscoveryLookupClient;
import com.proofpoint.discovery.client.DiscoveryModule;
import com.proofpoint.discovery.client.ServiceAnnouncement;
import com.proofpoint.discovery.client.ServiceDescriptor;
import com.proofpoint.discovery.client.ServiceSelector;
import com.proofpoint.discovery.client.ServiceSelectorConfig;
import com.proofpoint.discovery.client.testing.SimpleServiceSelector;
import com.proofpoint.discovery.monitor.DiscoveryEvent;
import com.proofpoint.discovery.monitor.DiscoveryEventType;
import com.proofpoint.discovery.monitor.DiscoveryStats;
import com.proofpoint.event.client.EventClient;
import com.proofpoint.event.client.InMemoryEventClient;
import com.proofpoint.event.client.InMemoryEventModule;
import com.proofpoint.http.server.testing.TestingHttpServer;
import com.proofpoint.http.server.testing.TestingHttpServerModule;
import com.proofpoint.jaxrs.JaxrsModule;
import com.proofpoint.json.JsonCodec;
import com.proofpoint.json.JsonModule;
import com.proofpoint.node.NodeInfo;
import com.proofpoint.node.NodeModule;
import org.iq80.leveldb.util.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.weakref.jmx.guice.MBeanModule;

import javax.management.MBeanServer;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;

import static com.proofpoint.json.JsonCodec.mapJsonCodec;
import static javax.ws.rs.core.Response.Status;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class TestDiscoveryServer
{
    private TestingHttpServer server;
    private File tempDir;
    private InMemoryEventClient inMemoryEventClient;
    private DiscoveryStats discoveryStats;

    @BeforeMethod
    public void setup()
            throws Exception
    {
        tempDir = Files.createTempDir();

        // start server
        Map<String, String> serverProperties = ImmutableMap.<String, String>builder()
                .put("node.environment", "testing")
                .put("static.db.location", tempDir.getAbsolutePath())
                .build();

        Injector serverInjector = Guice.createInjector(
                new MBeanModule(),
                new NodeModule(),
                new TestingHttpServerModule(),
                new JsonModule(),
                new JaxrsModule(),
                new DiscoveryServerModule(),
                new DiscoveryModule(),
                new InMemoryEventModule(),
                new ConfigurationModule(new ConfigurationFactory(serverProperties)),
                new Module()
                {
                    public void configure(Binder binder)
                    {
                        // TODO: use testing mbean server
                        binder.bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                    }
                });

        inMemoryEventClient = (InMemoryEventClient) serverInjector.getInstance(EventClient.class);
        discoveryStats = serverInjector.getInstance(DiscoveryStats.class);

        server = serverInjector.getInstance(TestingHttpServer.class);
        server.start();
    }

    @AfterMethod
    public void teardown()
            throws Exception
    {
        server.stop();
        FileUtils.deleteRecursively(tempDir);
    }

    @Test
    public void testDynamicAnnouncement()
            throws Exception
    {
        // publish announcement
        Map<String, String> announcerProperties = ImmutableMap.<String, String>builder()
                .put("node.environment", "testing")
                .put("node.pool", "red")
                .put("discovery.uri", server.getBaseUrl().toString())
                .build();

        Injector announcerInjector = Guice.createInjector(
                new NodeModule(),
                new JsonModule(),
                new ConfigurationModule(new ConfigurationFactory(announcerProperties)),
                new com.proofpoint.discovery.client.DiscoveryModule()
        );

        ServiceAnnouncement announcement = ServiceAnnouncement.serviceAnnouncement("apple")
                .addProperties(ImmutableMap.of("key", "value"))
                .build();

        DiscoveryAnnouncementClient client = announcerInjector.getInstance(DiscoveryAnnouncementClient.class);
        client.announce(ImmutableSet.of(announcement)).get();

        assertEquals(inMemoryEventClient.getEvents().size(), 1);

        DiscoveryEvent event = (DiscoveryEvent) inMemoryEventClient.getEvents().get(0);
        assertEquals(event.getType(), DiscoveryEventType.DYNAMICANNOUNCEMENT.name());
        assertNotNull(event.getRemoteAddress());
        assertTrue(event.getRequestUri().matches("http://.*/v1/announcement/.*"));
        assertTrue(event.getRequestBodyJson().matches("DynamicAnnouncement\\{environment=.*, pool=.*, location=.*, services=.*\\}"));

        assertEquals(discoveryStats.getDynamicAnnouncementSuccessCount(), 1);
        assertEquals(discoveryStats.getDynamicAnnouncementProcessingTime().getCount(), 1);

        NodeInfo announcerNodeInfo = announcerInjector.getInstance(NodeInfo.class);

        List<ServiceDescriptor> services = selectorFor("apple", "red").selectAllServices();
        assertEquals(services.size(), 1);

        ServiceDescriptor service = services.get(0);
        assertNotNull(service.getId());
        assertEquals(service.getNodeId(), announcerNodeInfo.getNodeId());
        assertEquals(service.getLocation(), announcerNodeInfo.getLocation());
        assertEquals(service.getPool(), announcerNodeInfo.getPool());
        assertEquals(service.getProperties(), announcement.getProperties());

        assertEquals(inMemoryEventClient.getEvents().size(), 2);

        event = (DiscoveryEvent) inMemoryEventClient.getEvents().get(1);
        assertEquals(event.getType(), DiscoveryEventType.SERVICEQUERY.name());
        assertNotNull(event.getRemoteAddress());
        assertTrue(event.getRequestUri().matches("http://.*/v1/service/apple/red"));
        assertEquals(event.getRequestBodyJson(), "");

        assertEquals(discoveryStats.getServiceQuerySuccessCount(), 1);
        assertEquals(discoveryStats.getServiceQueryProcessingTime().getCount(), 1);

        // ensure that service is no longer visible
        client.unannounce().get();

        assertEquals(inMemoryEventClient.getEvents().size(), 3);

        event = (DiscoveryEvent) inMemoryEventClient.getEvents().get(2);
        assertEquals(event.getType(), DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE.name());
        assertNotNull(event.getRemoteAddress());
        assertTrue(event.getRequestUri().matches("http://.*/v1/announcement/.*"));
        assertEquals(event.getRequestBodyJson(), "");

        assertEquals(discoveryStats.getDynamicAnnouncementDeleteSuccessCount(), 1);
        assertEquals(discoveryStats.getDynamicAnnouncementDeleteProcessingTime().getCount(), 1);

        assertTrue(selectorFor("apple", "red").selectAllServices().isEmpty());
        assertEquals(inMemoryEventClient.getEvents().size(), 4);

        event = (DiscoveryEvent) inMemoryEventClient.getEvents().get(3);
        assertEquals(event.getType(), DiscoveryEventType.SERVICEQUERY.name());
        assertNotNull(event.getRemoteAddress());
        assertTrue(event.getRequestUri().matches("http://.*/v1/service/apple/red"));
        assertEquals(event.getRequestBodyJson(), "");

        assertEquals(discoveryStats.getServiceQuerySuccessCount(), 2);
        assertEquals(discoveryStats.getServiceQueryProcessingTime().getCount(), 2);
    }


    @Test
    public void testStaticAnnouncement()
            throws Exception
    {
        // create static announcement
        Map<String, Object> announcement = ImmutableMap.<String, Object>builder()
                .put("environment", "testing")
                .put("type", "apple")
                .put("pool", "red")
                .put("location", "/a/b/c")
                .put("properties", ImmutableMap.of("http", "http://host"))
                .build();

        AsyncHttpClient httpClient = new AsyncHttpClient();
        Response response = httpClient.preparePost(server.getBaseUrl().resolve("/v1/announcement/static").toString())
                .addHeader("Content-Type", "application/json")
                .setBody(JsonCodec.jsonCodec(Object.class).toJson(announcement))
                .execute()
                .get();

        assertEquals(response.getStatusCode(), Status.CREATED.getStatusCode());
        String id = mapJsonCodec(String.class, Object.class)
                .fromJson(response.getResponseBody())
                .get("id")
                .toString();

        assertEquals(inMemoryEventClient.getEvents().size(), 1);

        DiscoveryEvent event = (DiscoveryEvent) inMemoryEventClient.getEvents().get(0);
        assertEquals(event.getType(), DiscoveryEventType.STATICANNOUNCEMENT.name());
        assertNotNull(event.getRemoteAddress());
        assertTrue(event.getRequestUri().matches("http://.*/v1/announcement/static"));
        assertTrue(event.getRequestBodyJson().matches("StaticAnnouncement\\{environment=.*, pool=.*, location=.*, type=.*, properties=\\{.*\\}"));

        assertEquals(discoveryStats.getStaticAnnouncementSuccessCount(), 1);
        assertEquals(discoveryStats.getStaticAnnouncementProcessingTime().getCount(), 1);

        List<ServiceDescriptor> services = selectorFor("apple", "red").selectAllServices();
        assertEquals(services.size(), 1);

        assertEquals(inMemoryEventClient.getEvents().size(), 2);

        event = (DiscoveryEvent) inMemoryEventClient.getEvents().get(1);
        assertEquals(event.getType(), DiscoveryEventType.SERVICEQUERY.name());
        assertNotNull(event.getRemoteAddress());
        assertTrue(event.getRequestUri().matches("http://.*/v1/service/apple/red"));
        assertEquals(event.getRequestBodyJson(), "");

        assertEquals(discoveryStats.getServiceQuerySuccessCount(), 1);
        assertEquals(discoveryStats.getServiceQueryProcessingTime().getCount(), 1);

        ServiceDescriptor service = services.get(0);
        assertEquals(service.getId().toString(), id);
        assertNull(service.getNodeId());
        assertEquals(service.getLocation(), announcement.get("location"));
        assertEquals(service.getPool(), announcement.get("pool"));
        assertEquals(service.getProperties(), announcement.get("properties"));

        // remove announcement
        response = httpClient.prepareDelete(server.getBaseUrl().resolve("/v1/announcement/static/" + id).toString())
                .execute()
                .get();

        assertEquals(response.getStatusCode(), Status.NO_CONTENT.getStatusCode());

        assertEquals(inMemoryEventClient.getEvents().size(), 3);

        event = (DiscoveryEvent) inMemoryEventClient.getEvents().get(2);
        assertEquals(event.getType(), DiscoveryEventType.STATICANNOUNCEMENTDELETE.name());
        assertNotNull(event.getRemoteAddress());
        assertTrue(event.getRequestUri().matches("http://.*/v1/announcement/static/.*"));
        assertEquals(event.getRequestBodyJson(), "");

        assertEquals(((DiscoveryEvent) inMemoryEventClient.getEvents().get(2)).getType(), DiscoveryEventType.STATICANNOUNCEMENTDELETE.name());
        assertEquals(discoveryStats.getStaticAnnouncementDeleteSuccessCount(), 1);
        assertEquals(discoveryStats.getStaticAnnouncementDeleteProcessingTime().getCount(), 1);

        // ensure announcement is gone
        assertTrue(selectorFor("apple", "red").selectAllServices().isEmpty());
        assertEquals(inMemoryEventClient.getEvents().size(), 4);

        event = (DiscoveryEvent) inMemoryEventClient.getEvents().get(3);
        assertEquals(event.getType(), DiscoveryEventType.SERVICEQUERY.name());
        assertNotNull(event.getRemoteAddress());
        assertTrue(event.getRequestUri().matches("http://.*/v1/service/apple/red"));
        assertEquals(event.getRequestBodyJson(), "");

        assertEquals(discoveryStats.getServiceQuerySuccessCount(), 2);
        assertEquals(discoveryStats.getServiceQueryProcessingTime().getCount(), 2);
    }

    private ServiceSelector selectorFor(String type, String pool)
    {
        Map<String, String> clientProperties = ImmutableMap.<String, String>builder()
                .put("node.environment", "testing")
                .put("discovery.uri", server.getBaseUrl().toString())
                .put("discovery.apple.pool", "red")
                .build();

        Injector clientInjector = Guice.createInjector(
                new NodeModule(),
                new JsonModule(),
                new ConfigurationModule(new ConfigurationFactory(clientProperties)),
                new com.proofpoint.discovery.client.DiscoveryModule()
        );

        DiscoveryLookupClient client = clientInjector.getInstance(DiscoveryLookupClient.class);
        return new SimpleServiceSelector(type, new ServiceSelectorConfig().setPool(pool), client);
    }
}
