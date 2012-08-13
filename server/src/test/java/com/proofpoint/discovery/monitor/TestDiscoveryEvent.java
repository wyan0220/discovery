package com.proofpoint.discovery.monitor;

import com.proofpoint.units.Duration;
import org.testng.annotations.Test;

import static com.proofpoint.testing.EquivalenceTester.equivalenceTester;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class TestDiscoveryEvent
{
    private final long startTime = System.nanoTime();

    @Test
    public void testNullArgument()
    {
        try {
            new DiscoveryEvent(null, true, "remote address", "request uri", Duration.nanosSince(startTime));
            fail();
        }
        catch (NullPointerException expected) {
        }
        try {
            new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, null, "request uri", Duration.nanosSince(startTime));
            fail();
        }
        catch (IllegalArgumentException expected) {
        }
        try {
            new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "", "request uri", Duration.nanosSince(startTime));
            fail();
        }
        catch (IllegalArgumentException expected) {
        }
        try {
            new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address", null, Duration.nanosSince(startTime));
            fail();
        }
        catch (IllegalArgumentException expected) {
        }
        try {
            new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address", "", Duration.nanosSince(startTime));
            fail();
        }
        catch (IllegalArgumentException expected) {
        }
        try {
            new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address", "request uri", null);
            fail();
        }
        catch (NullPointerException expected) {
        }
        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address", "request uri", Duration.nanosSince(startTime));
    }

    @Test
    public void testProperties()
    {
        Duration duration = Duration.nanosSince(startTime);
        DiscoveryEvent event = new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", duration);
        assertEquals(event.getType(), DiscoveryEventType.DYNAMICANNOUNCEMENT.name());
        assertEquals(event.isSuccess(), true);
        assertEquals(event.getRemoteAddress(), "remote address 1");
        assertEquals(event.getRequestUri(), "request uri 1");
        assertEquals(event.getProcessingDuration(), duration.toMillis());
    }

    @Test
    public void testEquivalence()
    {
        Duration duration1 = Duration.nanosSince(startTime);
        Duration duration2 = Duration.nanosSince(startTime);

        equivalenceTester()
                .addEquivalentGroup(new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", duration1),
                        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", duration1))
                .addEquivalentGroup(new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE, true, "remote address 1", "request uri 1", duration1),
                        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE, true, "remote address 1", "request uri 1", duration1))
                .addEquivalentGroup(new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, false, "remote address 1", "request uri 1", duration1),
                        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, false, "remote address 1", "request uri 1", duration1))
                .addEquivalentGroup(new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 2", "request uri 1", duration1),
                        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 2", duration1))
                .addEquivalentGroup(new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 2", duration1),
                        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 2", duration1))
                .addEquivalentGroup(new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", duration2),
                        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", duration2));
    }

    @Test
    public void testToString()
    {
        DiscoveryEvent event = new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", Duration.nanosSince(System.nanoTime() - 1000));
        assertTrue(event.toString().startsWith("DiscoveryEvent{type=DYNAMICANNOUNCEMENT, success=true, remoteAddress=remote address 1, requestUri=request uri 1, requestBodyJson=request body json, processingDuration="));
    }
}
