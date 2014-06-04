/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.impl;

import java.util.Map;
import java.util.Set;

import org.auraframework.def.DefDescriptor;
import org.auraframework.def.Definition;
import org.auraframework.def.DescriptorFilter;
import org.auraframework.system.MasterDefRegistry;
import org.auraframework.system.Source;
import org.auraframework.throwable.ClientOutOfSyncException;
import org.auraframework.throwable.quickfix.QuickFixException;

import com.google.common.collect.Maps;

/**
 * This is a fake registry (both master and standard) for testing purposes only.
 * Primarily used for testing validateReferences calls
 */
public class FakeRegistry implements MasterDefRegistry {
    private final Map<DefDescriptor<?>, Definition> stuff = Maps.newHashMap();

    public Definition putDefinition(Definition def) {
        return stuff.put(def.getDescriptor(), def);
    }

    public Definition removeDefinition(DefDescriptor<Definition> descriptor) {
        return stuff.remove(descriptor);
    }

    @Override
    public <D extends Definition> Set<DefDescriptor<D>> find(DefDescriptor<D> matcher) {
        return null;
    }

    @Override
    public Set<DefDescriptor<?>> find(DescriptorFilter matcher) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D extends Definition> D getDef(DefDescriptor<D> descriptor) {
        return (D) stuff.get(descriptor);
    }

    @Override
    public <D extends Definition> void save(D def) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <D extends Definition> boolean exists(DefDescriptor<D> descriptor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <D extends Definition> void addLocalDef(D def) {
    }

    @Override
    public <T extends Definition> Source<T> getSource(DefDescriptor<T> descriptor) {
        return null;
    }

    @Override
    public boolean namespaceExists(String ns) {
        return false;
    }

    @Override
    public void assertAccess(DefDescriptor<?> desc) {
    }

    @Override
    public Map<DefDescriptor<?>, Definition> filterRegistry(Set<DefDescriptor<?>> preloads) {
        return null;
    }

    @Override
    public <T extends Definition> boolean invalidate(DefDescriptor<T> descriptor) {
        return false;
    }

    @Override
    public <T extends Definition> String getUid(String uid, DefDescriptor<T> descriptor)
            throws ClientOutOfSyncException, QuickFixException {
        return null;
    }

    @Override
    public <T extends Definition> long getLastMod(String uid) {
        return 0;
    }

    @Override
    public <T extends Definition> Set<DefDescriptor<?>> getDependencies(String uid) {
        return null;
    }

    @Override
    public <T extends Definition> String getCachedString(String uid, DefDescriptor<?> descriptor, String key) {
        return null;
    }

    @Override
    public <T extends Definition> void putCachedString(String uid, DefDescriptor<?> descriptor, String key, String value) {
    }

    @Override
    public <T extends Definition> Map<DefDescriptor<?>, Integer> getDependenciesMap(String uid) {
        return null;
    }
}
