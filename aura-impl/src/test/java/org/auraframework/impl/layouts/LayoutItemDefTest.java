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
package org.auraframework.impl.layouts;

import org.auraframework.Aura;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.LayoutsDef;
import org.auraframework.impl.AuraImplTestCase;
import org.auraframework.throwable.quickfix.QuickFixException;

public class LayoutItemDefTest extends AuraImplTestCase {
    public LayoutItemDefTest(String name) {
        super(name);
    }

    public void testLayoutItemWithActionAndMarkup() throws Exception {
        DefDescriptor<LayoutsDef> dd = addSourceAutoCleanup(LayoutsDef.class,
                "<aura:layouts default='def'>"
                        + "<aura:layout name='def'>"
                        + "<aura:layoutItem container='target' action='{!c.act}'>text</aura:layoutItem>"
                        + "</aura:layout>"
                        + "</aura:layouts>");
        try {
            Aura.getDefinitionService().getDefinition(dd);
            fail("Expected QuickFixException");
        } catch (QuickFixException e) {
            assertEquals("layoutItem should have only either an action or markup but not both", e.getMessage());
        }
    }

    public void testLayoutItemWithoutActionOrMarkup() throws Exception {
        DefDescriptor<LayoutsDef> dd = addSourceAutoCleanup(LayoutsDef.class,
                "<aura:layouts default='def'>"
                        + "<aura:layout name='def'>" + "<aura:layoutItem container='target'/>" + "</aura:layout>"
                        + "</aura:layouts>");
        try {
            Aura.getDefinitionService().getDefinition(dd);
            fail("Expected QuickFixException");
        } catch (QuickFixException e) {
            assertEquals("layoutItem should have either an action or markup", e.getMessage());
        }
    }
}
