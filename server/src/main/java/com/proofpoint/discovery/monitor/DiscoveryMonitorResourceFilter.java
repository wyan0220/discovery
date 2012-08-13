package com.proofpoint.discovery.monitor;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;

public class DiscoveryMonitorResourceFilter implements ResourceFilter
{
    @Context
    private HttpServletRequest httpServletRequest;

    private final DiscoveryMonitor discoveryMonitor;

    @Inject
    public DiscoveryMonitorResourceFilter(DiscoveryMonitor discoveryMonitor)
    {
        this.discoveryMonitor = Preconditions.checkNotNull(discoveryMonitor);
    }

    @Override
    public ContainerRequestFilter getRequestFilter()
    {
        return new ContainerRequestFilter()
        {
            @Override
            public ContainerRequest filter(ContainerRequest request)
            {
                long startTime = System.nanoTime();
                request.getRequestHeaders().add("startTime", String.valueOf(startTime));

                return request;
            }
        };
    }

    @Override
    public ContainerResponseFilter getResponseFilter()
    {
        return new ContainerResponseFilter()
        {
            @Override
            public ContainerResponse filter(ContainerRequest request, ContainerResponse response)
            {
                ForMonitor forMonitorAnnotation = getAnnotation(response.getAnnotations());
                if (forMonitorAnnotation != null) {
                    boolean success = Ints.asList(forMonitorAnnotation.successCodes()).contains(response.getStatus());
                    discoveryMonitor.monitorDiscoveryEvent(forMonitorAnnotation.type(), success, httpServletRequest.getRemoteAddr(),
                            request.getRequestUri().toString(), Long.valueOf(request.getRequestHeader("startTIme").get(0)));
                }

                return response;
            }
        };
    }

    private ForMonitor getAnnotation(Annotation[] annotations)
    {
        for (Annotation annotation : annotations) {
            if (annotation instanceof ForMonitor) {
                return (ForMonitor) annotation;
            }
        }

        return null;
    }
}
