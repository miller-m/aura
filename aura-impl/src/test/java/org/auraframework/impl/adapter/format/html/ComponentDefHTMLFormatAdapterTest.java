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
package org.auraframework.impl.adapter.format.html;

import org.auraframework.Aura;
import org.auraframework.def.ComponentDef;
import org.auraframework.def.DefDescriptor;
import org.auraframework.system.AuraContext;
import org.auraframework.test.annotation.UnAdaptableTest;

/**
 * Tests for BaseComponentDefHTMLFormatAdapter, as it relates to ComponentDef
 * 
 * @since 0.0.224
 */
public class ComponentDefHTMLFormatAdapterTest extends BaseComponentDefHTMLFormatAdapterTest<ComponentDef> {

    public ComponentDefHTMLFormatAdapterTest(String name) {
        super(name);
    }

    @Override
    public Class<ComponentDef> getDefClass() {
        return ComponentDef.class;
    }

    /**
     * Don't append the manifest attribute to <html> when accessing components
     * directly.
     * 
     * TODO: re-enable after W-1166679
     */
    @UnAdaptableTest
    public void testWriteManifestWithPreloads() throws Exception {
        AuraContext context = Aura.getContextService().getCurrentContext();
        context.addPreload("aura");
        DefDescriptor<ComponentDef> desc = addSourceAutoCleanup(ComponentDef.class,
                "<aura:component render='client'></aura:component>");
        String body = doWrite(desc.getDef());
        int start = body.indexOf("<html ");
        String tag = body.substring(start, body.indexOf('>', start) + 1);
        if (tag.contains(" manifest=")) {
            fail("Should not have included a manifest attribute for components:\n" + body);
        }
    }
}
