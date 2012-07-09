package com.proofpoint.discovery.monitor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TestDiscoveryStats
{
    private DiscoveryStats discoveryStats;
    private long startTime;

    @BeforeMethod
    public void setup()
    {
        discoveryStats = new DiscoveryStats();
        startTime = System.nanoTime();
    }

    @Test
    public void testAddServiceQuerySuccessStats()
    {
        discoveryStats.addStats(DiscoveryEventType.SERVICEQUERY, true, startTime);
        assertEquals(discoveryStats.getServiceQuerySuccessCount(), 1);
        assertEquals(discoveryStats.getServiceQueryFailureCount(), 0);
        assertEquals(discoveryStats.getServiceQueryProcessingTime().getCount(), 1);
    }

    @Test
    public void testAddServiceQueryFailureStats()
    {
        discoveryStats.addStats(DiscoveryEventType.SERVICEQUERY, false, startTime);
        assertEquals(discoveryStats.getServiceQuerySuccessCount(), 0);
        assertEquals(discoveryStats.getServiceQueryFailureCount(), 1);
        assertEquals(discoveryStats.getServiceQueryProcessingTime().getCount(), 1);
    }

    @Test
    public void testAddStaticAnnouncementSuccessStats()
    {
        discoveryStats.addStats(DiscoveryEventType.STATICANNOUNCEMENT, true, startTime);
        assertEquals(discoveryStats.getStaticAnnouncementSuccessCount(), 1);
        assertEquals(discoveryStats.getStaticAnnouncementFailureCount(), 0);
        assertEquals(discoveryStats.getStaticAnnouncementProcessingTime().getCount(), 1);
    }

    @Test
    public void testAddStaticAnnouncementFailureStats()
    {
        discoveryStats.addStats(DiscoveryEventType.STATICANNOUNCEMENT, false, startTime);
        assertEquals(discoveryStats.getStaticAnnouncementSuccessCount(), 0);
        assertEquals(discoveryStats.getStaticAnnouncementFailureCount(), 1);
        assertEquals(discoveryStats.getStaticAnnouncementProcessingTime().getCount(), 1);
    }

    @Test
    public void testAddStaticAnnouncementListSuccessStats()
    {
        discoveryStats.addStats(DiscoveryEventType.STATICANNOUNCEMENTLIST, true, startTime);
        assertEquals(discoveryStats.getStaticAnnouncementListSuccessCount(), 1);
        assertEquals(discoveryStats.getStaticAnnouncementListFailureCount(), 0);
        assertEquals(discoveryStats.getStaticAnnouncementListProcessingTime().getCount(), 1);
    }

    @Test
    public void testAddStaticAnnouncementListFailureStats()
    {
        discoveryStats.addStats(DiscoveryEventType.STATICANNOUNCEMENTLIST, false, startTime);
        assertEquals(discoveryStats.getStaticAnnouncementListSuccessCount(), 0);
        assertEquals(discoveryStats.getStaticAnnouncementListFailureCount(), 1);
        assertEquals(discoveryStats.getStaticAnnouncementListProcessingTime().getCount(), 1);
    }

    @Test
    public void testAddStaticAnnouncementDeleteSuccessStats()
    {
        discoveryStats.addStats(DiscoveryEventType.STATICANNOUNCEMENTDELETE, true, startTime);
        assertEquals(discoveryStats.getStaticAnnouncementDeleteSuccessCount(), 1);
        assertEquals(discoveryStats.getStaticAnnouncementDeleteFailureCount(), 0);
        assertEquals(discoveryStats.getStaticAnnouncementDeleteProcessingTime().getCount(), 1);
    }

    @Test
    public void testAddStaticAnnouncementDeleteFailureStats()
    {
        discoveryStats.addStats(DiscoveryEventType.STATICANNOUNCEMENTDELETE, false, startTime);
        assertEquals(discoveryStats.getStaticAnnouncementDeleteSuccessCount(), 0);
        assertEquals(discoveryStats.getStaticAnnouncementDeleteFailureCount(), 1);
        assertEquals(discoveryStats.getStaticAnnouncementDeleteProcessingTime().getCount(), 1);
    }

    @Test
    public void testAddDynamicAnnouncementSuccessStats()
    {
        discoveryStats.addStats(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, startTime);
        assertEquals(discoveryStats.getDynamicAnnouncementSuccessCount(), 1);
        assertEquals(discoveryStats.getDynamicAnnouncementFailureCount(), 0);
        assertEquals(discoveryStats.getDynamicAnnouncementProcessingTime().getCount(), 1);
    }

    @Test
    public void testAddDynamicAnnouncementFailureStats()
    {
        discoveryStats.addStats(DiscoveryEventType.DYNAMICANNOUNCEMENT, false, startTime);
        assertEquals(discoveryStats.getDynamicAnnouncementSuccessCount(), 0);
        assertEquals(discoveryStats.getDynamicAnnouncementFailureCount(), 1);
        assertEquals(discoveryStats.getDynamicAnnouncementProcessingTime().getCount(), 1);
    }

    @Test
    public void testAddDynamicAnnouncementDeleteSuccessStats()
    {
        discoveryStats.addStats(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE, true, startTime);
        assertEquals(discoveryStats.getDynamicAnnouncementDeleteSuccessCount(), 1);
        assertEquals(discoveryStats.getDynamicAnnouncementDeleteFailureCount(), 0);
        assertEquals(discoveryStats.getDynamicAnnouncementDeleteProcessingTime().getCount(), 1);
    }

    @Test
    public void testAddDynamicAnnouncementDeleteFailureStats()
    {
        discoveryStats.addStats(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE, false, startTime);
        assertEquals(discoveryStats.getDynamicAnnouncementDeleteSuccessCount(), 0);
        assertEquals(discoveryStats.getDynamicAnnouncementDeleteFailureCount(), 1);
        assertEquals(discoveryStats.getDynamicAnnouncementDeleteProcessingTime().getCount(), 1);
    }

}
