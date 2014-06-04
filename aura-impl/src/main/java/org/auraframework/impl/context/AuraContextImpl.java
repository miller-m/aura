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
package org.auraframework.impl.context;

import java.io.IOException;
import java.util.*;

import javax.xml.stream.XMLStreamException;

import org.auraframework.Aura;
import org.auraframework.def.*;
import org.auraframework.def.DefDescriptor.DefType;
import org.auraframework.http.AuraBaseServlet;
import org.auraframework.instance.*;
import org.auraframework.system.*;
import org.auraframework.test.TestContext;
import org.auraframework.test.TestContextAdapter;
import org.auraframework.throwable.AuraUnhandledException;
import org.auraframework.throwable.quickfix.InvalidEventTypeException;
import org.auraframework.throwable.quickfix.QuickFixException;
import org.auraframework.util.AuraTextUtil;
import org.auraframework.util.json.*;
import org.auraframework.util.json.JsonSerializer.NoneSerializer;

import com.google.common.collect.*;

public class AuraContextImpl implements AuraContext {
    public static class SerializationContext extends BaseJsonSerializationContext {
        public SerializationContext() {
            super(false, false, -1, -1, false);
        }

        @SuppressWarnings("unchecked")
        @Override
        public JsonSerializer<?> getSerializer(Object o) {
            Class<?> c = o.getClass();
            if (c == AuraContextImpl.class || o instanceof AuraContextImpl) {
                return URL_SERIALIZER;
            } else if (c == ArrayList.class || o instanceof Collection) {
                return JsonSerializers.COLLECTION;
            } else if (c == Mode.class || c == String.class) {
                return JsonSerializers.STRING;
            }
            return null;
        }
    }

    private static class GlobalIdSorter implements Comparator<BaseComponent<?, ?>> {
        @Override
        public int compare(BaseComponent<?, ?> arg0, BaseComponent<?, ?> arg1) {
            String gid0 = arg0.getGlobalId();
            String gid1 = arg1.getGlobalId();
            List<String> gid0split = AuraTextUtil.splitSimple(":", gid0, 2);
            List<String> gid1split = AuraTextUtil.splitSimple(":", gid1, 2);
            return (Integer.parseInt(gid0split.get(gid0split.size() - 1))
                - Integer.parseInt(gid1split.get(gid1split.size() - 1)));
        }
    }

    private static final GlobalIdSorter GID_SORTER = new GlobalIdSorter();

    private static class Serializer extends NoneSerializer<AuraContext> {
        private final boolean forClient;

        private Serializer(boolean forClient) {
            this(forClient, false);
        }

        private Serializer(boolean forClient, boolean serializeLastMod) {
            this.forClient = forClient;
        }

        public static final String DELETED = "deleted";

        @Override
        public void serialize(Json json, AuraContext ctx) throws IOException {
            json.writeMapBegin();
            json.writeMapEntry("mode", ctx.getMode());

            DefDescriptor<? extends BaseComponentDef> appDesc = ctx.getApplicationDescriptor();
            if (appDesc != null) {
                if (appDesc.getDefType().equals(DefType.APPLICATION)) {
                    json.writeMapEntry("app", String.format("%s:%s", appDesc.getNamespace(), appDesc.getName()));
                } else {
                    json.writeMapEntry("cmp", String.format("%s:%s", appDesc.getNamespace(), appDesc.getName()));
                }
            }
            if (ctx.getSerializePreLoad()) {
                json.writeMapEntry("preloads", ctx.getPreloads());
            }
            if (ctx.getRequestedLocales() != null) {
                List<String> locales = new ArrayList<String>();
                for (Locale locale : ctx.getRequestedLocales()) {
                    locales.add(locale.toString());
                }
                json.writeMapEntry("requestedLocales", locales);
            }
            Map<String,String> loadedStrings = Maps.newHashMap();
            Map<DefDescriptor<?>, String> clientLoaded = Maps.newHashMap();
            clientLoaded.putAll(ctx.getClientLoaded());
            for (Map.Entry<DefDescriptor<?>,String> entry : ctx.getLoaded().entrySet()) {
                loadedStrings.put(String.format("%s@%s", entry.getKey().getDefType().toString(),
                                                entry.getKey().getQualifiedName()), entry.getValue());
                clientLoaded.remove(entry.getKey());
            }
            if (forClient) {
                for (DefDescriptor<?> deleted : clientLoaded.keySet()) {
                    loadedStrings.put(String.format("%s@%s", deleted.getDefType().toString(),
                                deleted.getQualifiedName()), DELETED);
                }
            }
            if (loadedStrings.size() > 0) {
                json.writeMapKey("loaded");
                json.writeMap(loadedStrings);
            }
            if (ctx.getSerializeLastMod()) {
                json.writeMapEntry("lastmod", Long.toString(AuraBaseServlet.getLastMod()));
            }

            TestContextAdapter testContextAdapter = Aura.get(TestContextAdapter.class);
            if (testContextAdapter != null) {
                TestContext testContext = testContextAdapter.getTestContext();
                if (testContext != null) {
                    json.writeMapEntry("test", testContext.getName());
                }
            }

            if (ctx.getFrameworkUID() != null) {
                json.writeMapEntry("fwuid", ctx.getFrameworkUID());
            }

            if (forClient) {
                // client needs value providers, urls don't
                boolean started = false;

                for (GlobalValueProvider valueProvider : ctx.getGlobalProviders().values()) {
                    if (!valueProvider.isEmpty()) {
                        if (!started) {
                            json.writeMapKey("globalValueProviders");
                            json.writeArrayBegin();
                            started = true;
                        }
                        json.writeComma();
                        json.writeIndent();
                        json.writeMapBegin();
                        json.writeMapEntry("type", valueProvider.getValueProviderKey().getPrefix());
                        json.writeMapEntry("values", valueProvider.getData());
                        json.writeMapEnd();
                    }
                }

                if (started) {
                    json.writeArrayEnd();
                }

                Map<String, BaseComponent<?, ?>> components = ctx.getComponents();
                if (!components.isEmpty()) {
                    List<BaseComponent<?, ?>> sorted = Lists.newArrayList(components.values());
                    Collections.sort(sorted, GID_SORTER);
                    json.writeMapKey("components");
                    json.writeMapBegin();

                    for (BaseComponent<?, ?> component : sorted) {
                        if (component.hasLocalDependencies()) {
                            json.writeMapEntry(component.getGlobalId(), component);
                        }
                    }

                    json.writeMapEnd();
                }
            }
            json.writeMapEnd();
        }
    }

    // serializer with everything for the client
    public static final Serializer FULL_SERIALIZER = new Serializer(true);

    // serializer just for passing context in a url
    public static final Serializer URL_SERIALIZER = new Serializer(false);

    // serializer just for passing context in a url
    public static final Serializer HTML_SERIALIZER = new Serializer(false, true);

    private final Set<DefDescriptor<?>> staleChecks = new HashSet<DefDescriptor<?>>();

    private final Mode mode;

    private final Access access;

    private final MasterDefRegistry masterRegistry;

    private final JsonSerializationContext jsonContext;

    private BaseComponent<?, ?> currentComponent;

    private Action currentAction;

    private final Map<DefType, String> defaultPrefixes;

    private String num;

    private String currentNamespace;

    private final LinkedHashSet<String> preloadedNamespaces = Sets.newLinkedHashSet();

    private final Format format;

    private final Map<ValueProviderType, GlobalValueProvider> globalProviders;

    private final Map<DefDescriptor<?>, String> loaded = Maps.newLinkedHashMap();
    private final Map<DefDescriptor<?>, String> clientLoaded = Maps.newLinkedHashMap();

    private final Map<String, BaseComponent<?, ?>> componentRegistry = Maps.newLinkedHashMap();

    private int nextId = 1;

    private String contextPath = "";

    private boolean serializePreLoad = true;

    private boolean serializeLastMod = true;

    private boolean preloading = false;

    private DefDescriptor<? extends BaseComponentDef> appDesc;

    private BaseComponentDef app;

    private boolean appLoaded = false;

    private DefDescriptor<?> preloadingDesc;

    private List<Locale> requestedLocales;

    private Client client = Client.OTHER;

    private String lastMod = "";

    private final List<Event> clientEvents = Lists.newArrayList();

    private String fwUID;
    
    private final boolean isDebugToolEnabled;

    public AuraContextImpl(Mode mode, MasterDefRegistry masterRegistry, Map<DefType, String> defaultPrefixes,
            Format format, Access access, JsonSerializationContext jsonContext,
            Map<ValueProviderType, GlobalValueProvider> globalProviders,
            DefDescriptor<? extends BaseComponentDef> appDesc, boolean isDebugToolEnabled) {
        if (access == Access.AUTHENTICATED) {
            preloadedNamespaces.add("aura");
            preloadedNamespaces.add("ui");
            if (mode == Mode.DEV) {
                preloadedNamespaces.add("auradev");
            }
        }
        this.mode = mode;
        this.masterRegistry = masterRegistry;
        this.defaultPrefixes = defaultPrefixes;
        this.format = format;
        this.access = access;
        this.jsonContext = jsonContext;
        this.globalProviders = globalProviders;
        this.appDesc = appDesc;
        this.isDebugToolEnabled = isDebugToolEnabled;
    }

    @Override
    public void addPreload(String preload) {
        preloadedNamespaces.add(preload);
    }

    @Override
    public void clearPreloads() {
        preloadedNamespaces.clear();
    }

    @Override
    public boolean isPreloaded(DefDescriptor<?> descriptor) {
        if (preloading) {
            return false;
        }
        if (appDesc != null && !appLoaded) {
            appLoaded = true;
            try {
                app = masterRegistry.getDef(appDesc);
            } catch (QuickFixException qfe) {
                // we just don't have an app, ignore this.
            } catch (AuraUnhandledException ahe) {
                // Ugh! our file has been created, but not written?
                // TODO: W-1486796
                if (!(ahe.getCause() instanceof XMLStreamException)) {
                    throw ahe;
                }
            }
        }
        if (app != null) {
            for (DependencyDef dd : app.getDependencies()) {
                if (dd.getDependency().matchDescriptor(descriptor)) {
                    return true;
                }
            }
        }
        return preloadedNamespaces.contains(descriptor.getNamespace());
    }

    @Override
    public Access getAccess() {
        return access;
    }

    @Override
    public DefDescriptor<? extends BaseComponentDef> getApplicationDescriptor() {
        return appDesc;
    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public Map<String, BaseComponent<?, ?>> getComponents() {
        return componentRegistry;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public Action getCurrentAction() {
        return currentAction;
    }

    @Override
    public BaseComponent<?, ?> getCurrentComponent() {
        return currentComponent;
    }

    @Override
    public String getCurrentNamespace() {
        return currentNamespace;
    }

    @Override
    public String getDefaultPrefix(DefType defType) {
        return defaultPrefixes.get(defType);
    }

    @Override
    public MasterDefRegistry getDefRegistry() {
        return masterRegistry;
    }

    @Override
    public Format getFormat() {
        return format;
    }

    @Override
    public Map<ValueProviderType, GlobalValueProvider> getGlobalProviders() {
        return globalProviders;
    }

    @Override
    public JsonSerializationContext getJsonSerializationContext() {
        return jsonContext;
    }

    @Override
    public String getLastMod() {
        return lastMod;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public int getNextId() {
        return nextId++;
    }

    @Override
    public String getNum() {
        return num;
    }

    @Override
    public Set<String> getPreloads() {
        return Collections.unmodifiableSet(preloadedNamespaces);
    }

    @Override
    public List<Locale> getRequestedLocales() {
        return requestedLocales;
    }

    @Override
    public boolean getSerializeLastMod() {
        return serializeLastMod;
    }

    @Override
    public boolean getSerializePreLoad() {
        return serializePreLoad;
    }

    @Override
    public boolean hasChecked(DefDescriptor<?> d) {
        return staleChecks.contains(d);
    }

    @Override
    public boolean isPreloading() {
        return preloading;
    }

    @Override
    public boolean isTestMode() {
        return getMode().isTestMode();
    }

    @Override
    public boolean isDevMode() {
        return getMode().isDevMode();
    }

    @Override
    public void registerComponent(BaseComponent<?, ?> component) {
        Action action = getCurrentAction();
        if (action != null) {
            action.registerComponent(component);
        } else {
            componentRegistry.put(component.getGlobalId(), component);
        }
    }

    @Override
    public void setApplicationDescriptor(DefDescriptor<? extends BaseComponentDef> appDesc) {
        //
        // This logic is twisted, but not unreasonable. If someone is setting an application,
        // we use it, otherwise, if it is a Component, we only override components, leaving
        // applications intact. Since components are only legal for dev mode, this shouldn't
        // affect much. In fact, most use cases, this.appDesc will be null.
        //
        if ((appDesc != null && appDesc.getDefType().equals(DefType.APPLICATION)) || this.appDesc == null
                || !this.appDesc.getDefType().equals(DefType.APPLICATION)) {
            this.appDesc = appDesc;
        }
    }

    @Override
    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void setContextPath(String path) {
        this.contextPath = path;
    }

    @Override
    public Action setCurrentAction(Action nextAction) {
        Action old = currentAction;
        currentAction = nextAction;
        return old;
    }

    @Override
    public BaseComponent<?, ?> setCurrentComponent(BaseComponent<?, ?> nextComponent) {
        BaseComponent<?, ?> old = currentComponent;
        currentComponent = nextComponent;
        return old;
    }

    @Override
    public void setCurrentNamespace(String namespace) {
        this.currentNamespace = namespace;
    }

    @Override
    public void setLastMod(String lastMod) {
        this.lastMod = lastMod;
    }

    @Override
    public void setNum(String num) {
        this.num = num;
    }

    @Override
    public void setPreloading(boolean preloading) {
        this.preloading = preloading;
    }

    @Override
    public void setRequestedLocales(List<Locale> requestedLocales) {
        this.requestedLocales = requestedLocales;
    }

    @Override
    public void setSerializeLastMod(boolean serializeLastMod) {
        this.serializeLastMod = serializeLastMod;
    }

    @Override
    public void setSerializePreLoad(boolean serializePreLoad) {
        this.serializePreLoad = serializePreLoad;
    }

    @Override
    public void setStaleCheck(DefDescriptor<?> d) {
        staleChecks.add(d);
    }

    @Override
    public void addClientApplicationEvent(Event event) throws Exception {
        if (event != null) {
            if (event.getDescriptor().getDef().getEventType() != EventType.APPLICATION) {
                throw new InvalidEventTypeException(
                        String.format("%s is not an Application event. "
                                + "Only Application events are allowed to be fired from server.",
                                event.getDescriptor()), null);
            }
            clientEvents.add(event);
        }
    }

    @Override
    public List<Event> getClientEvents() {
        return clientEvents;
    }

    @Override
    public void setClientLoaded(Map<DefDescriptor<?>, String> clientLoaded) {
        loaded.putAll(clientLoaded);
        clientLoaded.putAll(clientLoaded);
    }

    @Override
    public void addLoaded(DefDescriptor<?> descriptor, String uid) {
        loaded.put(descriptor, uid);
    }


    @Override
    public void dropLoaded(DefDescriptor<?> descriptor) {
        loaded.remove(descriptor);
    }

    @Override
    public Map<DefDescriptor<?>, String> getClientLoaded() {
        return Collections.unmodifiableMap(clientLoaded);
    }

    @Override
    public Map<DefDescriptor<?>, String> getLoaded() {
        return Collections.unmodifiableMap(loaded);
    }

    @Override
    public void setPreloading(DefDescriptor<?> descriptor) {
        preloadingDesc = descriptor;
    }

    @Override
    public DefDescriptor<?> getPreloading() {
        return preloadingDesc;
    }

    @Override
    public String getUid(DefDescriptor<?> descriptor) {
        return loaded.get(descriptor);
    }

    @Override
    public void setFrameworkUID(String uid) {
        this.fwUID = uid;
    }

    @Override
    public String getFrameworkUID() {
        return fwUID;
    }

    @Override
    public boolean getIsDebugToolEnabled() {
            return isDebugToolEnabled;
    }
}
