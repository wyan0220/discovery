package com.proofpoint.discovery.monitor;

import org.testng.annotations.Test;

import static com.proofpoint.testing.EquivalenceTester.equivalenceTester;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class TestDiscoveryFailureEvent
{
    private final long startTime = System.nanoTime();

    @Test
    public void testNullArgument()
    {
        try {
            new DiscoveryFailureEvent(null, new ArrayIndexOutOfBoundsException(), "request uri");
            fail();
        }
        catch (NullPointerException expected) {
        }
        try {
            new DiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, null, "request uri");
            fail();
        }
        catch (NullPointerException expected) {
        }
        try {
            new DiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, new ArrayIndexOutOfBoundsException(), null);
            fail();
        }
        catch (IllegalArgumentException expected) {
        }
        try {
            new DiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, new ArrayIndexOutOfBoundsException(), "");
            fail();
        }
        catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testProperties()
    {
        Exception exception = new ArrayIndexOutOfBoundsException();
        DiscoveryFailureEvent event = new DiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, exception, "request uri 1");
        assertEquals(event.getType(), DiscoveryEventType.DYNAMICANNOUNCEMENT.name());
        assertEquals(event.getException(), exception.getMessage());
        assertEquals(event.getRequestUri(), "request uri 1");
    }

    @Test
    public void testEquivalence()
    {
        equivalenceTester()
                .addEquivalentGroup(new DiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, new ArrayIndexOutOfBoundsException(), "request uri 1"),
                        new DiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, new ArrayIndexOutOfBoundsException(), "request uri 1"))
                .addEquivalentGroup(new DiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE, new ArrayIndexOutOfBoundsException(), "request uri 1"),
                        new DiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENTDELETE, new ArrayIndexOutOfBoundsException(), "request uri 1"))
                .addEquivalentGroup(new DiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, new NullPointerException(), "request uri 1"),
                        new DiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, new NullPointerException(), "request uri 1"))
                .addEquivalentGroup(new DiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, new ArrayIndexOutOfBoundsException(), "request uri 2"),
                        new DiscoveryFailureEvent(DiscoveryEventType.DYNAMICANNOUNCEMENT, new ArrayIndexOutOfBoundsException(), "request uri 2"));
    }
}
