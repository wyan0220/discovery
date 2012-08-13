package com.proofpoint.discovery.monitor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ForMonitor
{
    DiscoveryEventType type();
    int[] successCodes();
}