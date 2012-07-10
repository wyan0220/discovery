package com.proofpoint.discovery.monitor;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.proofpoint.event.client.EventClient;
import com.proofpoint.units.Duration;

import java.util.concurrent.TimeUnit;

public class DiscoveryMonitor
{
    private final EventClient eventClient;
    private final DiscoveryStats stats;

    @Inject
    public DiscoveryMonitor(EventClient eventClient, DiscoveryStats stats)
    {
        this.eventClient = Preconditions.checkNotNull(eventClient);
        this.stats = Preconditions.checkNotNull(stats);
    }

    public void monitorDiscoveryEvent(DiscoveryEventType type, boolean success, String remoteAddress, String requestUri, String requestBodyJson, long startTime)
    {
        eventClient.post(new DiscoveryEvent(type, success, remoteAddress, requestUri,
                requestBodyJson, new Duration(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)));
        stats.addStats(type, success, startTime);
    }

    public void monitorDiscoveryFailureEvent(DiscoveryEventType type, Exception e, String requestUri)
    {
        eventClient.post(new DiscoveryFailureEvent(type, e, requestUri));
    }
}
