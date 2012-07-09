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
            new DiscoveryEvent(null, true, "remote address", "request uri", "request body", Duration.nanosSince(startTime));
            fail();
        }
        catch (NullPointerException expected) {
        }
        try {
            new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, null, "request uri", "request body", Duration.nanosSince(startTime));
            fail();
        }
        catch (IllegalArgumentException expected) {
        }
        try {
            new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "", "request uri", "request body", Duration.nanosSince(startTime));
            fail();
        }
        catch (IllegalArgumentException expected) {
        }
        try {
            new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address", null, "request body", Duration.nanosSince(startTime));
            fail();
        }
        catch (IllegalArgumentException expected) {
        }
        try {
            new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address", "", "request body", Duration.nanosSince(startTime));
            fail();
        }
        catch (IllegalArgumentException expected) {
        }
        try {
            new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address", "request uri", "request body", null);
            fail();
        }
        catch (NullPointerException expected) {
        }
        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address", "request uri", null, Duration.nanosSince(startTime));
    }

    @Test
    public void testProperties()
    {
        Duration duration = Duration.nanosSince(startTime);
        DiscoveryEvent event = new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", "request body json", duration);
        assertEquals(event.getType(), DiscoveryEventType.DYNAMICANNOUNCEMENT);
        assertEquals(event.isSuccess(), true);
        assertEquals(event.getRemoteAddress(), "remote address 1");
        assertEquals(event.getRequestUri(), "request uri 1");
        assertEquals(event.getRequestBodyJson(), "request body json");
        assertEquals(event.getProcessingDuration(), duration);
    }

    @Test
    public void testEquivalence()
    {
        Duration duration1 = Duration.nanosSince(startTime);
        Duration duration2 = Duration.nanosSince(startTime);

        equivalenceTester()
                .addEquivalentGroup(new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", "request body json", duration1),
                        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", "request body json", duration1))
                .addEquivalentGroup(new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE, true, "remote address 1", "request uri 1", "request body json", duration1),
                        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE, true, "remote address 1", "request uri 1", "request body json", duration1))
                .addEquivalentGroup(new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, false, "remote address 1", "request uri 1", "request body json", duration1),
                        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, false, "remote address 1", "request uri 1", "request body json", duration1))
                .addEquivalentGroup(new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 2", "request uri 1", "request body json", duration1),
                        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 2", "request body json", duration1))
                .addEquivalentGroup(new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 2", "request body json", duration1),
                        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 2", "request body json", duration1))
                .addEquivalentGroup(new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", "request body json 2", duration1),
                        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", "request body json 2", duration1))
                .addEquivalentGroup(new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", "request body json", duration2),
                        new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", "request body json", duration2));
    }

    @Test
    public void testToString()
    {
        DiscoveryEvent event = new DiscoveryEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, true, "remote address 1", "request uri 1", "request body json", Duration.nanosSince(System.nanoTime() - 1000));
        assertTrue(event.toString().startsWith("DiscoveryEvent{type:DYNAMICANNOUNCEMENT,success:true,remoteAddress:remote address 1,requestUri:request uri 1,requestBodyJson:request body json,processingDuration:"));
    }
}
