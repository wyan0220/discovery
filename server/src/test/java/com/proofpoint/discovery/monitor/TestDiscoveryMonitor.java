package com.proofpoint.discovery.monitor;

import com.proofpoint.event.client.InMemoryEventClient;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TestDiscoveryMonitor
{
    private InMemoryEventClient eventClient;
    private DiscoveryStats stats;
    private DiscoveryMonitor monitor;
    private long startTime;
    private final String remoteAddress = "http://m0010111.lab.ppops.net";
    private final String requestUri = "http://localhost:4111";

    @BeforeMethod
    public void setup()
    {
        eventClient = new InMemoryEventClient();
        stats = new DiscoveryStats();
        monitor = new DiscoveryMonitor(eventClient, stats);
        startTime = System.nanoTime();
    }

    @Test
    public void testMonitorServiceQueryEvent()
    {
        monitor.monitorDiscoveryEvent(DiscoveryEventType.SERVICEQUERY, true, remoteAddress, requestUri, "", startTime);
        assertEquals(stats.getServiceQuerySuccessCount(), 1);
        assertEquals(stats.getServiceQueryProcessingTime().getCount(), 1);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.SERVICEQUERY.name());
    }

    @Test
    public void testMonitorStaticAnnouncementEvent()
    {
        monitor.monitorDiscoveryEvent(DiscoveryEventType.STATICANNOUNCEMENT, false, remoteAddress, requestUri, "", startTime);
        assertEquals(stats.getStaticAnnouncementFailureCount(), 1);
        assertEquals(stats.getStaticAnnouncementProcessingTime().getCount(), 1);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.STATICANNOUNCEMENT.name());
    }

    @Test
    public void testMonitorStaticAnnouncementListEvent()
    {
        monitor.monitorDiscoveryEvent(DiscoveryEventType.STATICANNOUNCEMENTLIST, false, remoteAddress, requestUri, "", startTime);
        assertEquals(stats.getStaticAnnouncementListFailureCount(), 1);
        assertEquals(stats.getStaticAnnouncementListProcessingTime().getCount(), 1);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.STATICANNOUNCEMENTLIST.name());
    }

    @Test
    public void testMonitorStaticAnnouncementDeleteEvent()
    {
        monitor.monitorDiscoveryEvent(DiscoveryEventType.STATICANNOUNCEMENTDELETE, true, remoteAddress, requestUri, "", startTime);
        assertEquals(stats.getStaticAnnouncementDeleteSuccessCount(), 1);
        assertEquals(stats.getStaticAnnouncementDeleteProcessingTime().getCount(), 1);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.STATICANNOUNCEMENTDELETE.name());
    }

    @Test
    public void testMonitorDynamicAnnouncementEvent()
    {
        monitor.monitorDiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, false, remoteAddress, requestUri, "", startTime);
        assertEquals(stats.getDynamicAnnouncementFailureCount(), 1);
        assertEquals(stats.getDynamicAnnouncementProcessingTime().getCount(), 1);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.DYNAMICANNOUNCEMENT.name());
    }

    @Test
    public void testMonitorDynamicAnnouncementDeleteEvent()
    {
        monitor.monitorDiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE, true, remoteAddress, requestUri, "", startTime);
        assertEquals(stats.getDynamicAnnouncementDeleteSuccessCount(), 1);
        assertEquals(stats.getDynamicAnnouncementDeleteProcessingTime().getCount(), 1);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE.name());
    }

    @Test
    public void testMonitorServiceQueryFailureEvent()
    {
        monitor.monitorDiscoveryFailureEvent(DiscoveryEventType.SERVICEQUERY, new NullPointerException(), requestUri);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryFailureEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.SERVICEQUERY.name());
    }

    @Test
    public void testMonitorStaticAnnouncementFailureEvent()
    {
        monitor.monitorDiscoveryFailureEvent(DiscoveryEventType.STATICANNOUNCEMENT, new NullPointerException(), requestUri);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryFailureEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.STATICANNOUNCEMENT.name());
    }

    @Test
    public void testMonitorStaticAnnouncementListFailureEvent()
    {
        monitor.monitorDiscoveryFailureEvent(DiscoveryEventType.STATICANNOUNCEMENTLIST, new NullPointerException(), requestUri);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryFailureEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.STATICANNOUNCEMENTLIST.name());
    }

    @Test
    public void testMonitorStaticAnnouncementDeleteFailureEvent()
    {
        monitor.monitorDiscoveryFailureEvent(DiscoveryEventType.STATICANNOUNCEMENTDELETE, new NullPointerException(), requestUri);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryFailureEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.STATICANNOUNCEMENTDELETE.name());
    }

    @Test
    public void testMonitorDynamicAnnouncementFailureEvent()
    {
        monitor.monitorDiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, new NullPointerException(), requestUri);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryFailureEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.DYNAMICANNOUNCEMENT.name());
    }

    @Test
    public void testMonitorDynamicAnnouncementDeleteFailureEvent()
    {
        monitor.monitorDiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE, new NullPointerException(), requestUri);
        assertEquals(eventClient.getEvents().size(), 1);
        assertEquals(((DiscoveryFailureEvent) eventClient.getEvents().get(0)).getType(), DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE.name());
    }
}
