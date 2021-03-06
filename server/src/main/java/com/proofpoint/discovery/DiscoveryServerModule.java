/*
 * Copyright 2010 Proofpoint, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.proofpoint.discovery;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.proofpoint.discovery.client.ServiceDescriptor;
import com.proofpoint.discovery.client.ServiceInventory;
import com.proofpoint.discovery.client.ServiceSelector;
import com.proofpoint.discovery.store.InMemoryStore;
import com.proofpoint.discovery.store.PersistentStore;
import com.proofpoint.discovery.store.PersistentStoreConfig;
import com.proofpoint.discovery.store.ReplicatedStoreModule;
import com.proofpoint.node.NodeInfo;
import org.weakref.jmx.MBeanExporter;

import javax.inject.Singleton;
import javax.management.MBeanServer;
import java.util.List;

import static com.proofpoint.configuration.ConfigurationModule.bindConfig;
import static com.proofpoint.discovery.client.DiscoveryBinder.discoveryBinder;

public class DiscoveryServerModule
        implements Module
{
    public void configure(Binder binder)
    {
        bindConfig(binder).to(DiscoveryConfig.class);
        binder.bind(ServiceResource.class).in(Scopes.SINGLETON);

        discoveryBinder(binder).bindHttpAnnouncement("discovery");

        // dynamic announcements
        binder.bind(DynamicAnnouncementResource.class).in(Scopes.SINGLETON);
        binder.bind(DynamicStore.class).to(ReplicatedDynamicStore.class).in(Scopes.SINGLETON);
        binder.install(new ReplicatedStoreModule("dynamic", ForDynamicStore.class, InMemoryStore.class));

        // static announcements
        binder.bind(StaticAnnouncementResource.class).in(Scopes.SINGLETON);
        binder.bind(StaticStore.class).to(ReplicatedStaticStore.class).in(Scopes.SINGLETON);
        binder.install(new ReplicatedStoreModule("static", ForStaticStore.class, PersistentStore.class));
        bindConfig(binder).prefixedWith("static").to(PersistentStoreConfig.class);
    }

    @Singleton
    @Provides
    public MBeanExporter getMBeanExporter(MBeanServer mbeanServer)
    {
        // TODO: get rid of this once we upgrade to jmxutils 1.11+
        return new MBeanExporter(mbeanServer);
    }

    @Singleton
    @Provides
    public ServiceSelector getServiceInventory(final ServiceInventory inventory, final NodeInfo nodeInfo)
    {
        return new ServiceSelector()
        {
            @Override
            public String getType()
            {
                return "discovery";
            }

            @Override
            public String getPool()
            {
                return nodeInfo.getPool();
            }

            @Override
            public List<ServiceDescriptor> selectAllServices()
            {
                return ImmutableList.copyOf(inventory.getServiceDescriptors(getType()));
            }
        };
    }
}
