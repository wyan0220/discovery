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
import com.proofpoint.discovery.monitor.DiscoveryMonitorResourceFilter;
import com.proofpoint.discovery.monitor.ForMonitor;
import com.proofpoint.node.NodeInfo;
import com.sun.jersey.spi.container.ResourceFilters;

import javax.inject.Inject;
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

import static com.proofpoint.discovery.monitor.DiscoveryEventType.STATICANNOUNCEMENT;
import static com.proofpoint.discovery.monitor.DiscoveryEventType.STATICANNOUNCEMENTDELETE;
import static com.proofpoint.discovery.monitor.DiscoveryEventType.STATICANNOUNCEMENTLIST;
import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/v1/announcement/static")
@ResourceFilters(DiscoveryMonitorResourceFilter.class)
public class StaticAnnouncementResource
{
    private final StaticStore store;
    private final NodeInfo nodeInfo;

    @Inject
    public StaticAnnouncementResource(StaticStore store, NodeInfo nodeInfo)
    {
        this.store = store;
        this.nodeInfo = nodeInfo;
    }

    @POST
    @Consumes("application/json")
    @ForMonitor(type = STATICANNOUNCEMENT, successCodes = {201})
    public Response post(@Context UriInfo uriInfo, StaticAnnouncement announcement)
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

    @GET
    @Produces("application/json")
    @ForMonitor(type = STATICANNOUNCEMENTLIST, successCodes = {200})
    public Services get()
    {
        return new Services(nodeInfo.getEnvironment(), store.getAll());
    }

    @DELETE
    @Path("{id}")
    @ForMonitor(type = STATICANNOUNCEMENTDELETE, successCodes = {204})
    public void delete(@PathParam("id") final Id<Service> id)
    {
        store.delete(id);
    }

}
