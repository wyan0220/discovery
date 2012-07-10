package com.proofpoint.discovery.monitor;

import com.proofpoint.stats.TimedStat;
import com.proofpoint.units.Duration;
import org.weakref.jmx.Managed;
import org.weakref.jmx.Nested;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicLong;

public class DiscoveryStats
{
    private final EnumMap<DiscoveryEventType, Stats> eventTypeStatsEnumMap;

    public DiscoveryStats()
    {
        eventTypeStatsEnumMap = new EnumMap<DiscoveryEventType, Stats>(DiscoveryEventType.class);
        for (DiscoveryEventType type : DiscoveryEventType.values()) {
            eventTypeStatsEnumMap.put(type, new Stats());
        }
    }

    @Managed
    public long getServiceQuerySuccessCount()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.SERVICEQUERY).getSuccessCount();
    }

    @Managed
    public long getServiceQueryFailureCount()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.SERVICEQUERY).getFailureCount();
    }

    @Managed
    public long getStaticAnnouncementSuccessCount()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.STATICANNOUNCEMENT).getSuccessCount();
    }

    @Managed
    public long getStaticAnnouncementFailureCount()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.STATICANNOUNCEMENT).getFailureCount();
    }

    @Managed
    public long getStaticAnnouncementListSuccessCount()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.STATICANNOUNCEMENTLIST).getSuccessCount();
    }

    @Managed
    public long getStaticAnnouncementListFailureCount()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.STATICANNOUNCEMENTLIST).getFailureCount();
    }

    @Managed
    public long getStaticAnnouncementDeleteSuccessCount()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.STATICANNOUNCEMENTDELETE).getSuccessCount();
    }

    @Managed
    public long getStaticAnnouncementDeleteFailureCount()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.STATICANNOUNCEMENTDELETE).getFailureCount();
    }

    @Managed
    public long getDynamicAnnouncementSuccessCount()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.DYNAMICANNOUNCEMENT).getSuccessCount();
    }

    @Managed
    public long getDynamicAnnouncementFailureCount()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.DYNAMICANNOUNCEMENT).getFailureCount();
    }

    @Managed
    public long getDynamicAnnouncementDeleteSuccessCount()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE).getSuccessCount();
    }

    @Managed
    public long getDynamicAnnouncementDeleteFailureCount()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE).getFailureCount();
    }

    @Managed
    @Nested
    public TimedStat getServiceQueryProcessingTime()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.SERVICEQUERY).getProcessingTime();
    }

    @Managed
    @Nested
    public TimedStat getStaticAnnouncementProcessingTime()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.STATICANNOUNCEMENT).getProcessingTime();
    }

    @Managed
    @Nested
    public TimedStat getStaticAnnouncementListProcessingTime()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.STATICANNOUNCEMENTLIST).getProcessingTime();
    }

    @Managed
    @Nested
    public TimedStat getStaticAnnouncementDeleteProcessingTime()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.STATICANNOUNCEMENTDELETE).getProcessingTime();
    }

    @Managed
    @Nested
    public TimedStat getDynamicAnnouncementProcessingTime()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.DYNAMICANNOUNCEMENT).getProcessingTime();
    }

    @Managed
    @Nested
    public TimedStat getDynamicAnnouncementDeleteProcessingTime()
    {
        return eventTypeStatsEnumMap.get(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE).getProcessingTime();
    }

    public void addStats(DiscoveryEventType type, boolean success, long startTime)
    {
        eventTypeStatsEnumMap.get(type).incrementCount(success);
        eventTypeStatsEnumMap.get(type).addProcessingTime(startTime);
    }

    private static class Stats
    {
        private final AtomicLong successCount;
        private final AtomicLong failureCount;
        private final TimedStat processingTime;

        public Stats()
        {
            this.successCount = new AtomicLong();
            this.failureCount = new AtomicLong();
            this.processingTime = new TimedStat();
        }

        public long getSuccessCount()
        {
            return successCount.get();
        }

        public long getFailureCount()
        {
            return failureCount.get();
        }

        public TimedStat getProcessingTime()
        {
            return processingTime;
        }

        public void incrementCount(boolean success)
        {
            if (success) {
                successCount.getAndIncrement();
            }
            else {
                failureCount.getAndIncrement();
            }
        }

        public void addProcessingTime(long startTime)
        {
            processingTime.addValue(Duration.nanosSince(startTime));
        }
    }
}
