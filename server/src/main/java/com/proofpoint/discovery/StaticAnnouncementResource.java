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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.proofpoint.discovery.monitor.DiscoveryEventType;
import com.proofpoint.discovery.monitor.DiscoveryMonitor;
import com.proofpoint.node.NodeInfo;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/v1/announcement/static")
public class StaticAnnouncementResource
{
    private final StaticStore store;
    private final NodeInfo nodeInfo;
    private final DiscoveryMonitor discoveryMonitor;

    @Inject
    public StaticAnnouncementResource(StaticStore store, NodeInfo nodeInfo, DiscoveryMonitor discoveryMonitor)
    {
        this.store = store;
        this.nodeInfo = nodeInfo;
        this.discoveryMonitor = Preconditions.checkNotNull(discoveryMonitor);
    }

    @POST
    @Consumes("application/json")
    public Response post(@Context HttpServletRequest httpServletRequest, @Context final UriInfo uriInfo, final StaticAnnouncement announcement)
    {
        EventMonitorWrapper<Response> eventMonitor = new EventMonitorWrapper<Response>(discoveryMonitor, DiscoveryEventType.STATICANNOUNCEMENT, uriInfo, httpServletRequest, announcement.toString())
        {
            @Override
            public Response doWork()
            {
                if (!nodeInfo.getEnvironment().equals(announcement.getEnvironment())) {
                    return Response.status(BAD_REQUEST)
                            .entity(format("Environment mismatch. Expected: %s, Provided: %s", nodeInfo.getEnvironment(), announcement.getEnvironment()))
                            .build();
                }

                Id<Service> id = Id.random();
                String location = Objects.firstNonNull(announcement.getLocation(), "/somewhere/" + id);

                Service service = Service.copyOf(announcement)
                        .setId(id)
                        .setLocation(location)
                        .build();

                store.put(service);

                URI uri = UriBuilder.fromUri(uriInfo.getBaseUri()).path(StaticAnnouncementResource.class).path("{id}").build(id);
                return Response.created(uri).entity(service).build();
            }
        };
        return eventMonitor.monitor();
    }

    @GET
    @Produces("application/json")
    public Services get(@Context HttpServletRequest httpServletRequest, @Context UriInfo uriInfo)
    {
        EventMonitorWrapper<Services> eventMonitor = new EventMonitorWrapper<Services>(discoveryMonitor, DiscoveryEventType.STATICANNOUNCEMENTLIST, uriInfo, httpServletRequest, "")
        {
            @Override
            public Services doWork()
            {
                return new Services(nodeInfo.getEnvironment(), store.getAll());
            }
        };
        return eventMonitor.monitor();
    }

    @DELETE
    @Path("{id}")
    public void delete(@Context HttpServletRequest httpServletRequest, @Context UriInfo uriInfo, @PathParam("id") final Id<Service> id)
    {
        EventMonitorWrapper<Void> eventMonitor = new EventMonitorWrapper<Void>(discoveryMonitor, DiscoveryEventType.STATICANNOUNCEMENTDELETE, uriInfo, httpServletRequest, "")
        {
            @Override
            public Void doWork()
            {
                store.delete(id);
                return null;
            }
        };
        eventMonitor.monitor();
    }
}
