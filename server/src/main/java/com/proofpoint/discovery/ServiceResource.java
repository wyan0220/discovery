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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.proofpoint.discovery.monitor.DiscoveryEventType;
import com.proofpoint.discovery.monitor.DiscoveryMonitor;
import com.proofpoint.node.NodeInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import static com.google.common.collect.Sets.union;


@Path("/v1/service")
public class ServiceResource
{
    private final DynamicStore dynamicStore;
    private final StaticStore staticStore;
    private final NodeInfo node;
    private final DiscoveryMonitor discoveryMonitor;

    @Inject
    public ServiceResource(DynamicStore dynamicStore, StaticStore staticStore, NodeInfo node, DiscoveryMonitor discoveryMonitor)
    {
        this.dynamicStore = dynamicStore;
        this.staticStore = staticStore;
        this.node = node;
        this.discoveryMonitor = Preconditions.checkNotNull(discoveryMonitor);
    }

    @GET
    @Path("{type}/{pool}")
    @Produces(MediaType.APPLICATION_JSON)
    public Services getServices(@Context HttpServletRequest httpServletRequest, @Context UriInfo uriInfo, @PathParam("type") String type, @PathParam("pool") String pool)
    {
        boolean success = true;
        long startTime = System.nanoTime();
        try {
            return new Services(node.getEnvironment(), union(dynamicStore.get(type, pool), staticStore.get(type, pool)));
        }
        catch (RuntimeException e) {
            success = false;
            discoveryMonitor.monitorDiscoveryFailureEvent(DiscoveryEventType.SERVICEQUERY, e, uriInfo.getRequestUri().toString());
            throw e;
        }
        finally {
            discoveryMonitor.monitorDiscoveryEvent(DiscoveryEventType.SERVICEQUERY, success, httpServletRequest.getRemoteAddr(),
                    uriInfo.getRequestUri().toString(), "", startTime);
        }
    }

    @GET
    @Path("{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Services getServices(@Context HttpServletRequest httpServletRequest, @Context UriInfo uriInfo, @PathParam("type") String type)
    {
        boolean success = true;
        long startTime = System.nanoTime();
        try {
            return new Services(node.getEnvironment(), union(dynamicStore.get(type), staticStore.get(type)));
        }
        catch (RuntimeException e) {
            success = false;
            discoveryMonitor.monitorDiscoveryFailureEvent(DiscoveryEventType.SERVICEQUERY, e, uriInfo.getRequestUri().toString());
            throw e;
        }
        finally {
            discoveryMonitor.monitorDiscoveryEvent(DiscoveryEventType.SERVICEQUERY, success, httpServletRequest.getRemoteAddr(),
                    uriInfo.getRequestUri().toString(), "", startTime);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Services getServices(@Context HttpServletRequest httpServletRequest, @Context UriInfo uriInfo)
    {
        boolean success = true;
        long startTime = System.nanoTime();
        try {
            return new Services(node.getEnvironment(), union(dynamicStore.getAll(), staticStore.getAll()));
        }
        catch (RuntimeException e) {
            success = false;
            discoveryMonitor.monitorDiscoveryFailureEvent(DiscoveryEventType.SERVICEQUERY, e, uriInfo.getRequestUri().toString());
            throw e;
        }
        finally {
            discoveryMonitor.monitorDiscoveryEvent(DiscoveryEventType.SERVICEQUERY, success, httpServletRequest.getRemoteAddr(),
                    uriInfo.getRequestUri().toString(), "", startTime);
        }
    }
}
