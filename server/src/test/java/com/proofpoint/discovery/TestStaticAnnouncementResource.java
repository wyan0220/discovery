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
import com.proofpoint.discovery.monitor.DiscoveryEvent;
import com.proofpoint.discovery.monitor.DiscoveryEventType;
import com.proofpoint.discovery.monitor.DiscoveryMonitor;
import com.proofpoint.discovery.monitor.DiscoveryStats;
import com.proofpoint.event.client.InMemoryEventClient;
import com.proofpoint.jaxrs.testing.MockUriInfo;
import com.proofpoint.node.NodeInfo;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class TestStaticAnnouncementResource
{
    private InMemoryStaticStore store;
    private StaticAnnouncementResource resource;
    private InMemoryEventClient eventClient;
    private DiscoveryStats discoveryStats;
    private HttpServletRequest httpServletRequest;
    private final UriInfo uriInfo = MockUriInfo.from("http://localhost:4111/v1/announcement/static");

    @BeforeMethod
    public void setup()
    {
        store = new InMemoryStaticStore();
        eventClient = new InMemoryEventClient();
        discoveryStats = new DiscoveryStats();
        httpServletRequest = Mockito.mock(HttpServletRequest.class);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        resource = new StaticAnnouncementResource(store, new NodeInfo("testing"), new DiscoveryMonitor(eventClient, discoveryStats));
    }

    @Test
    public void testPost()
    {
        StaticAnnouncement announcement = new StaticAnnouncement("testing", "storage", "alpha", "/a/b", ImmutableMap.of("http", "http://localhost:1111"));

        Response response = resource.post(httpServletRequest, uriInfo, announcement);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());

        assertEquals(store.getAll().size(), 1);
        Service service = store.getAll().iterator().next();

        assertNotNull(service.getId());
        assertNull(service.getNodeId());
        assertEquals(service.getLocation(), announcement.getLocation());
        assertEquals(service.getType(), announcement.getType());
        assertEquals(service.getPool(), announcement.getPool());
        assertEquals(service.getProperties(), announcement.getProperties());

        assertEquals(discoveryStats.getStaticAnnouncementSuccessCount(), 1);
        assertEquals(discoveryStats.getStaticAnnouncementFailureCount(), 0);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent)eventClient.getEvents().get(0)).getType(), DiscoveryEventType.STATICANNOUNCEMENT);
    }

    @Test
    public void testEnvironmentConflict()
    {
        StaticAnnouncement announcement = new StaticAnnouncement("production", "storage", "alpha", "/a/b/c", ImmutableMap.of("http", "http://localhost:1111"));

        Response response = resource.post(httpServletRequest, uriInfo, announcement);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

        assertTrue(store.getAll().isEmpty());

        assertEquals(discoveryStats.getStaticAnnouncementSuccessCount(), 1);
        assertEquals(discoveryStats.getStaticAnnouncementFailureCount(), 0);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent)eventClient.getEvents().get(0)).getType(), DiscoveryEventType.STATICANNOUNCEMENT);
    }

    @Test
    public void testDelete()
    {
        Service blue = new Service(Id.<Service>random(), null, "storage", "alpha", "/a/b/c", ImmutableMap.of("key", "valueBlue"));
        Service red = new Service(Id.<Service>random(), null, "storage", "alpha", "/a/b/c", ImmutableMap.of("key", "valueRed"));

        store.put(red);
        store.put(blue);

        resource.delete(httpServletRequest, uriInfo, blue.getId());
        assertEquals(store.getAll(), ImmutableSet.of(red));

        assertEquals(discoveryStats.getStaticAnnouncementDeleteSuccessCount(), 1);
        assertEquals(discoveryStats.getStaticAnnouncementDeleteFailureCount(), 0);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent)eventClient.getEvents().get(0)).getType(), DiscoveryEventType.STATICANNOUNCEMENTDELETE);
    }

    @Test
    public void testMakesUpLocation()
    {
        StaticAnnouncement announcement = new StaticAnnouncement("testing", "storage", "alpha", null, ImmutableMap.of("http", "http://localhost:1111"));

        Response response = resource.post(httpServletRequest, uriInfo, announcement);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());

        assertEquals(store.getAll().size(), 1);
        Service service = store.getAll().iterator().next();
        assertEquals(service.getId(), service.getId());
        assertNotNull(service.getLocation());

        assertEquals(discoveryStats.getStaticAnnouncementSuccessCount(), 1);
        assertEquals(discoveryStats.getStaticAnnouncementFailureCount(), 0);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent)eventClient.getEvents().get(0)).getType(), DiscoveryEventType.STATICANNOUNCEMENT);
    }

    @Test
    public void testGet()
    {
        Service blue = new Service(Id.<Service>random(), null, "storage", "alpha", "/a/b/c", ImmutableMap.of("key", "valueBlue"));
        Service red = new Service(Id.<Service>random(), null, "storage", "alpha", "/a/b/c", ImmutableMap.of("key", "valueRed"));

        store.put(red);
        store.put(blue);

        Services actual = resource.get(httpServletRequest, uriInfo);
        Services expected = new Services("testing", ImmutableSet.of(red, blue));

        assertEquals(actual, expected);

        assertEquals(discoveryStats.getStaticAnnouncementListSuccessCount(), 1);
        assertEquals(discoveryStats.getStaticAnnouncementListFailureCount(), 0);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent)eventClient.getEvents().get(0)).getType(), DiscoveryEventType.STATICANNOUNCEMENTLIST);
    }
}
