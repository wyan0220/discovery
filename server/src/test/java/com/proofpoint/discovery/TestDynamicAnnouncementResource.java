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
import com.proofpoint.discovery.store.RealTimeProvider;
import com.proofpoint.event.client.InMemoryEventClient;
import com.proofpoint.jaxrs.testing.MockUriInfo;
import com.proofpoint.node.NodeInfo;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.google.common.collect.Iterables.transform;
import static com.proofpoint.discovery.DynamicServiceAnnouncement.toServiceWith;
import static com.proofpoint.testing.Assertions.assertEqualsIgnoreOrder;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TestDynamicAnnouncementResource
{
    private InMemoryDynamicStore store;
    private DynamicAnnouncementResource resource;
    private InMemoryEventClient eventClient;
    private DiscoveryStats discoveryStats;
    private HttpServletRequest httpServletRequest;
    private final UriInfo uriInfo = MockUriInfo.from("http://localhost:8080/v1/announcement/");

    @BeforeMethod
    public void setup()
    {
        eventClient = new InMemoryEventClient();
        discoveryStats = new DiscoveryStats();
        httpServletRequest = Mockito.mock(HttpServletRequest.class);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        store = new InMemoryDynamicStore(new DiscoveryConfig(), new RealTimeProvider());
        resource = new DynamicAnnouncementResource(store, new NodeInfo("testing"), new DiscoveryMonitor(eventClient, discoveryStats));
    }

    @Test
    public void testPutNew()
    {
        DynamicAnnouncement announcement = new DynamicAnnouncement("testing", "alpha", "/a/b/c", ImmutableSet.of(
                new DynamicServiceAnnouncement(Id.<Service>random(), "storage", ImmutableMap.of("http", "http://localhost:1111")))
        );

        Id<Node> nodeId = Id.random();
        Response response = resource.put(httpServletRequest, uriInfo, nodeId, announcement);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.ACCEPTED.getStatusCode());

        assertEqualsIgnoreOrder(store.getAll(), transform(announcement.getServiceAnnouncements(), toServiceWith(nodeId, announcement.getLocation(), announcement.getPool())));

        assertEquals(discoveryStats.getDynamicAnnouncementSuccessCount(), 1);
        assertEquals(discoveryStats.getDynamicAnnouncementFailureCount(), 0);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.DYNAMICANNOUNCEMENT);
    }

    @Test
    public void testReplace()
    {
        Id<Node> nodeId = Id.random();
        DynamicAnnouncement previous = new DynamicAnnouncement("testing", "alpha", "/a/b/c", ImmutableSet.of(
                new DynamicServiceAnnouncement(Id.<Service>random(), "storage", ImmutableMap.of("key", "existing"))
        ));

        store.put(nodeId, previous);

        DynamicAnnouncement announcement = new DynamicAnnouncement("testing", "alpha", "/a/b/c", ImmutableSet.of(
                new DynamicServiceAnnouncement(Id.<Service>random(), "storage", ImmutableMap.of("key", "new")))
        );

        Response response = resource.put(httpServletRequest, uriInfo, nodeId, announcement);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.ACCEPTED.getStatusCode());

        assertEqualsIgnoreOrder(store.getAll(), transform(announcement.getServiceAnnouncements(), toServiceWith(nodeId, announcement.getLocation(), announcement.getPool())));

        assertEquals(discoveryStats.getDynamicAnnouncementSuccessCount(), 1);
        assertEquals(discoveryStats.getDynamicAnnouncementFailureCount(), 0);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.DYNAMICANNOUNCEMENT);
    }

    @Test
    public void testEnvironmentConflict()
    {
        DynamicAnnouncement announcement = new DynamicAnnouncement("production", "alpha", "/a/b/c", ImmutableSet.of(
                new DynamicServiceAnnouncement(Id.<Service>random(), "storage", ImmutableMap.of("http", "http://localhost:1111")))
        );

        Id<Node> nodeId = Id.random();
        Response response = resource.put(httpServletRequest, uriInfo, nodeId, announcement);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

        assertTrue(store.getAll().isEmpty());

        assertEquals(discoveryStats.getDynamicAnnouncementSuccessCount(), 1);
        assertEquals(discoveryStats.getDynamicAnnouncementFailureCount(), 0);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.DYNAMICANNOUNCEMENT);
    }

    @Test
    public void testDeleteExisting()
    {
        Id<Node> blueNodeId = Id.random();
        DynamicAnnouncement blue = new DynamicAnnouncement("testing", "alpha", "/a/b/c", ImmutableSet.of(
                new DynamicServiceAnnouncement(Id.<Service>random(), "storage", ImmutableMap.of("key", "valueBlue"))
        ));

        Id<Node> redNodeId = Id.random();
        DynamicAnnouncement red = new DynamicAnnouncement("testing", "alpha", "/a/b/c", ImmutableSet.of(
                new DynamicServiceAnnouncement(Id.<Service>random(), "storage", ImmutableMap.of("key", "valueBlue"))
        ));

        store.put(redNodeId, red);
        store.put(blueNodeId, blue);

        Response response = resource.delete(httpServletRequest, uriInfo, blueNodeId);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        assertEqualsIgnoreOrder(store.getAll(), transform(red.getServiceAnnouncements(), toServiceWith(redNodeId, red.getLocation(), red.getPool())));

        assertEquals(discoveryStats.getDynamicAnnouncementDeleteSuccessCount(), 1);
        assertEquals(discoveryStats.getDynamicAnnouncementDeleteFailureCount(), 0);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE);
    }

    @Test
    public void testDeleteMissing()
    {
        Response response = resource.delete(httpServletRequest, uriInfo, Id.<Node>random());

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());

        assertTrue(store.getAll().isEmpty());

        assertEquals(discoveryStats.getDynamicAnnouncementDeleteSuccessCount(), 1);
        assertEquals(discoveryStats.getDynamicAnnouncementDeleteFailureCount(), 0);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE);
    }

    @Test
    public void testMakesUpLocation()
    {
        DynamicAnnouncement announcement = new DynamicAnnouncement("testing", "alpha", null, ImmutableSet.of(
                new DynamicServiceAnnouncement(Id.<Service>random(), "storage", ImmutableMap.of("http", "http://localhost:1111")))
        );

        Id<Node> nodeId = Id.random();
        Response response = resource.put(httpServletRequest, uriInfo, nodeId, announcement);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.ACCEPTED.getStatusCode());

        assertEquals(store.getAll().size(), 1);
        Service service = store.getAll().iterator().next();
        assertEquals(service.getId(), service.getId());
        assertNotNull(service.getLocation());

        assertEquals(discoveryStats.getDynamicAnnouncementSuccessCount(), 1);
        assertEquals(discoveryStats.getDynamicAnnouncementFailureCount(), 0);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.DYNAMICANNOUNCEMENT);
    }
}
