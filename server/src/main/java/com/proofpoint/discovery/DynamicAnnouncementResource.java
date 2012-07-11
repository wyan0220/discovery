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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/v1/announcement/{node_id}")
public class DynamicAnnouncementResource
{
    private final NodeInfo nodeInfo;
    private final DynamicStore dynamicStore;
    private final DiscoveryMonitor discoveryMonitor;

    @Inject
    public DynamicAnnouncementResource(DynamicStore dynamicStore, NodeInfo nodeInfo, DiscoveryMonitor discoveryMonitor)
    {
        this.dynamicStore = dynamicStore;
        this.nodeInfo = nodeInfo;
        this.discoveryMonitor = Preconditions.checkNotNull(discoveryMonitor);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(@Context HttpServletRequest httpServletRequest, @Context UriInfo uriInfo, @PathParam("node_id") Id<Node> nodeId, DynamicAnnouncement announcement)
    {
        boolean success = true;
        long startTime = System.nanoTime();
        try {
            if (!nodeInfo.getEnvironment().equals(announcement.getEnvironment())) {
                return Response.status(BAD_REQUEST)
                        .entity(format("Environment mismatch. Expected: %s, Provided: %s", nodeInfo.getEnvironment(), announcement.getEnvironment()))
                        .build();
            }

            String location = Objects.firstNonNull(announcement.getLocation(), "/somewhere/" + nodeId.toString());

            DynamicAnnouncement announcementWithLocation = DynamicAnnouncement.copyOf(announcement)
                    .setLocation(location)
                    .build();

            dynamicStore.put(nodeId, announcementWithLocation);

            return Response.status(ACCEPTED).build();
        }
        catch (RuntimeException e) {
            success = false;
            discoveryMonitor.monitorDiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, e, uriInfo.getRequestUri().toString());
            throw e;
        }
        finally {
            discoveryMonitor.monitorDiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, success, httpServletRequest.getRemoteAddr(),
                    uriInfo.getRequestUri().toString(), announcement.toString(), startTime);
        }
    }

    @DELETE
    public Response delete(@Context HttpServletRequest httpServletRequest, @Context UriInfo uriInfo, @PathParam("node_id") Id<Node> nodeId)
    {
        boolean success = true;
        long startTime = System.nanoTime();
        try {
            if (!dynamicStore.delete(nodeId)) {
                return Response.status(NOT_FOUND).build();
            }

            return Response.noContent().build();
        }
        catch (RuntimeException e) {
            success = false;
            discoveryMonitor.monitorDiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE, e, uriInfo.getRequestUri().toString());
            throw e;
        }
        finally {
            discoveryMonitor.monitorDiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE, success, httpServletRequest.getRemoteAddr(),
                    uriInfo.getRequestUri().toString(), "", startTime);
        }
    }
}
