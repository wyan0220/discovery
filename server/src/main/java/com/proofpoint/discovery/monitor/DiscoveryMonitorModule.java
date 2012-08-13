package com.proofpoint.discovery.monitor;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;
import org.weakref.jmx.guice.MBeanModule;

import static com.proofpoint.event.client.EventBinder.eventBinder;

public class DiscoveryMonitorModule implements Module
{
    @Override
    public void configure(Binder binder)
    {
        binder.bind(DiscoveryMonitor.class).in(Scopes.SINGLETON);
        binder.bind(DiscoveryStats.class).in(Scopes.SINGLETON);
        binder.bind(DiscoveryMonitorResourceFilter.class).in(Scopes.SINGLETON);
        MBeanModule.newExporter(binder).export(DiscoveryStats.class).withGeneratedName();

        // event
        eventBinder(binder).bindEventClient(DiscoveryEvent.class);
        eventBinder(binder).bindEventClient(DiscoveryFailureEvent.class);
    }
}
