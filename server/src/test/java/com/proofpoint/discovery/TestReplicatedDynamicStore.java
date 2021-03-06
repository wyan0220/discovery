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

import com.proofpoint.discovery.store.ConflictResolver;
import com.proofpoint.discovery.store.DistributedStore;
import com.proofpoint.discovery.store.Entry;
import com.proofpoint.discovery.store.InMemoryStore;
import com.proofpoint.discovery.store.RemoteStore;
import com.proofpoint.discovery.store.StoreConfig;
import org.joda.time.DateTime;

import javax.inject.Provider;

public class TestReplicatedDynamicStore
    extends TestDynamicStore
{
    @Override
    protected DynamicStore initializeStore(DiscoveryConfig config, Provider<DateTime> timeProvider)
    {
        RemoteStore dummy = new RemoteStore() {
            public void put(Entry entry) { }
        };

        DistributedStore distributedStore = new DistributedStore("dynamic", new InMemoryStore(new ConflictResolver()), dummy, new StoreConfig(), timeProvider);

        return new ReplicatedDynamicStore(distributedStore, config);
    }
}
