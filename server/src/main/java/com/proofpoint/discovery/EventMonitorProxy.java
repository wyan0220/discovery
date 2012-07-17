package com.proofpoint.discovery;

import com.proofpoint.discovery.monitor.DiscoveryEventType;
import com.proofpoint.discovery.monitor.DiscoveryMonitor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

public abstract class EventMonitorProxy<T>
{
    private final DiscoveryMonitor discoveryMonitor;
    private final DiscoveryEventType type;
    private final UriInfo uriInfo;
    private final HttpServletRequest httpServletRequest;
    private final String requestBody;

    public EventMonitorProxy(DiscoveryMonitor discoveryMonitor, DiscoveryEventType type, UriInfo uriInfo, HttpServletRequest httpServletRequest, String requestBody)
    {
        this.discoveryMonitor = discoveryMonitor;
        this.type = type;
        this.uriInfo = uriInfo;
        this.httpServletRequest = httpServletRequest;
        this.requestBody = requestBody;
    }

    public T execute()
    {
        boolean success = true;
        long startTime = System.nanoTime();
        try {
            return doWork();
        }
        catch (RuntimeException e) {
            success = false;
            discoveryMonitor.monitorDiscoveryFailureEvent(type, e, uriInfo.getRequestUri().toString());
            throw e;
        }
        finally {
            discoveryMonitor.monitorDiscoveryEvent(type, success, httpServletRequest.getRemoteAddr(),
                    uriInfo.getRequestUri().toString(), requestBody, startTime);
        }
    }
    
    public abstract T doWork();
}
