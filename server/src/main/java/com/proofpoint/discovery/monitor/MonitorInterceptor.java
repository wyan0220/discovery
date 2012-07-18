package com.proofpoint.discovery.monitor;

import com.google.inject.Inject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

public class MonitorInterceptor implements MethodInterceptor
{
    @Inject
    DiscoveryMonitor discoveryMonitor;

    @Override
    public Object invoke(MethodInvocation methodInvocation)
            throws Throwable
    {
        boolean success = false;
        long startTime = System.nanoTime();
        DiscoveryEventType type = methodInvocation.getMethod().getAnnotation(MonitorWith.class).value();
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInvocation.getArguments()[0];
        UriInfo uriInfo = (UriInfo) methodInvocation.getArguments()[1];
        String requestBody = (type == DiscoveryEventType.DYNAMICANNOUNCEMENT || type == DiscoveryEventType.STATICANNOUNCEMENT) ?
                (methodInvocation.getArguments()[2].toString()) : "";
        try {
            Object result = methodInvocation.proceed();
            success = true;
            return result;
        }
        catch (RuntimeException e) {
            discoveryMonitor.monitorDiscoveryFailureEvent(type, e, uriInfo.getRequestUri().toString());
            throw e;
        }
        finally {
            discoveryMonitor.monitorDiscoveryEvent(type, success, httpServletRequest.getRemoteAddr(),
                    uriInfo.getRequestUri().toString(), requestBody, startTime);
        }
    }
}
