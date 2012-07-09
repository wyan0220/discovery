package com.proofpoint.discovery.monitor;

import com.proofpoint.stats.TimedStat;
import com.proofpoint.units.Duration;
import org.weakref.jmx.Managed;
import org.weakref.jmx.Nested;

import java.util.concurrent.atomic.AtomicLong;

public class DiscoveryStats
{
    private final AtomicLong serviceQuerySuccessCount = new AtomicLong();
    private final AtomicLong serviceQueryFailureCount = new AtomicLong();
    private final AtomicLong staticAnnouncementSuccessCount = new AtomicLong();
    private final AtomicLong staticAnnouncementFailureCount = new AtomicLong();
    private final AtomicLong staticAnnouncementListSuccessCount = new AtomicLong();
    private final AtomicLong staticAnnouncementListFailureCount = new AtomicLong();
    private final AtomicLong staticAnnouncementDeleteSuccessCount = new AtomicLong();
    private final AtomicLong staticAnnouncementDeleteFailureCount = new AtomicLong();
    private final AtomicLong dynamicAnnouncementSuccessCount = new AtomicLong();
    private final AtomicLong dynamicAnnouncementFailureCount = new AtomicLong();
    private final AtomicLong dynamicAnnouncementDeleteSuccessCount = new AtomicLong();
    private final AtomicLong dynamicAnnouncementDeleteFailureCount = new AtomicLong();

    private final TimedStat serviceQueryProcessingTime = new TimedStat();
    private final TimedStat staticAnnouncementProcessingTime = new TimedStat();
    private final TimedStat staticAnnouncementListProcessingTime = new TimedStat();
    private final TimedStat staticAnnouncementDeleteProcessingTime = new TimedStat();
    private final TimedStat dynamicAnnouncementProcessingTime = new TimedStat();
    private final TimedStat dynamicAnnouncementDeleteProcessingTime = new TimedStat();

    @Managed
    public long getServiceQuerySuccessCount()
    {
        return serviceQuerySuccessCount.get();
    }

    @Managed
    public long getServiceQueryFailureCount()
    {
        return serviceQueryFailureCount.get();
    }

    @Managed
    public long getStaticAnnouncementSuccessCount()
    {
        return staticAnnouncementSuccessCount.get();
    }

    @Managed
    public long getStaticAnnouncementFailureCount()
    {
        return staticAnnouncementFailureCount.get();
    }

    @Managed
    public long getStaticAnnouncementListSuccessCount()
    {
        return staticAnnouncementListSuccessCount.get();
    }

    @Managed
    public long getStaticAnnouncementListFailureCount()
    {
        return staticAnnouncementListFailureCount.get();
    }

    @Managed
    public long getStaticAnnouncementDeleteSuccessCount()
    {
        return staticAnnouncementDeleteSuccessCount.get();
    }

    @Managed
    public long getStaticAnnouncementDeleteFailureCount()
    {
        return staticAnnouncementDeleteFailureCount.get();
    }

    @Managed
    public long getDynamicAnnouncementSuccessCount()
    {
        return dynamicAnnouncementSuccessCount.get();
    }

    @Managed
    public long getDynamicAnnouncementFailureCount()
    {
        return dynamicAnnouncementFailureCount.get();
    }

    @Managed
    public long getDynamicAnnouncementDeleteSuccessCount()
    {
        return dynamicAnnouncementDeleteSuccessCount.get();
    }

    @Managed
    public long getDynamicAnnouncementDeleteFailureCount()
    {
        return dynamicAnnouncementDeleteFailureCount.get();
    }

    @Managed
    @Nested
    public TimedStat getServiceQueryProcessingTime()
    {
        return serviceQueryProcessingTime;
    }

    @Managed
    @Nested
    public TimedStat getStaticAnnouncementProcessingTime()
    {
        return staticAnnouncementProcessingTime;
    }

    @Managed
    @Nested
    public TimedStat getStaticAnnouncementListProcessingTime()
    {
        return staticAnnouncementListProcessingTime;
    }

    @Managed
    @Nested
    public TimedStat getStaticAnnouncementDeleteProcessingTime()
    {
        return staticAnnouncementDeleteProcessingTime;
    }

    @Managed
    @Nested
    public TimedStat getDynamicAnnouncementProcessingTime()
    {
        return dynamicAnnouncementProcessingTime;
    }

    @Managed
    @Nested
    public TimedStat getDynamicAnnouncementDeleteProcessingTime()
    {
        return dynamicAnnouncementDeleteProcessingTime;
    }

    public void addStats(DiscoveryEventType type, boolean success, long startTime)
    {
        if (success) {
            switch (type) {
                case STATICANNOUNCEMENT:
                    staticAnnouncementSuccessCount.getAndIncrement();
                    staticAnnouncementProcessingTime.addValue(Duration.nanosSince(startTime));
                    break;
                case STATICANNOUNCEMENTLIST:
                    staticAnnouncementListSuccessCount.getAndIncrement();
                    staticAnnouncementListProcessingTime.addValue(Duration.nanosSince(startTime));
                    break;
                case STATICANNOUNCEMENTDELETE:
                    staticAnnouncementDeleteSuccessCount.getAndIncrement();
                    staticAnnouncementDeleteProcessingTime.addValue(Duration.nanosSince(startTime));
                    break;
                case DYNAMICANNOUNCEMENT:
                    dynamicAnnouncementSuccessCount.getAndIncrement();
                    dynamicAnnouncementProcessingTime.addValue(Duration.nanosSince(startTime));
                    break;
                case DYNAMICANNOUNCEMENTDELETE:
                    dynamicAnnouncementDeleteSuccessCount.getAndIncrement();
                    dynamicAnnouncementDeleteProcessingTime.addValue(Duration.nanosSince(startTime));
                    break;
                case SERVICEQUERY:
                    serviceQuerySuccessCount.getAndIncrement();
                    serviceQueryProcessingTime.addValue(Duration.nanosSince(startTime));
                    break;
            }
        }
        else {
            switch (type) {
                case STATICANNOUNCEMENT:
                    staticAnnouncementFailureCount.getAndIncrement();
                    staticAnnouncementProcessingTime.addValue(Duration.nanosSince(startTime));
                    break;
                case STATICANNOUNCEMENTLIST:
                    staticAnnouncementListFailureCount.getAndIncrement();
                    staticAnnouncementListProcessingTime.addValue(Duration.nanosSince(startTime));
                    break;
                case STATICANNOUNCEMENTDELETE:
                    staticAnnouncementDeleteFailureCount.getAndIncrement();
                    staticAnnouncementDeleteProcessingTime.addValue(Duration.nanosSince(startTime));
                    break;
                case DYNAMICANNOUNCEMENT:
                    dynamicAnnouncementFailureCount.getAndIncrement();
                    dynamicAnnouncementProcessingTime.addValue(Duration.nanosSince(startTime));
                    break;
                case DYNAMICANNOUNCEMENTDELETE:
                    dynamicAnnouncementDeleteFailureCount.getAndIncrement();
                    dynamicAnnouncementDeleteProcessingTime.addValue(Duration.nanosSince(startTime));
                    break;
                case SERVICEQUERY:
                    serviceQueryFailureCount.getAndIncrement();
                    serviceQueryProcessingTime.addValue(Duration.nanosSince(startTime));
                    break;
            }
        }
    }
}
