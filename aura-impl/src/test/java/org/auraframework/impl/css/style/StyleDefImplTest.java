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
package org.auraframework.impl.css.style;

import java.util.Map;
import java.util.Set;

import org.auraframework.Aura;
import org.auraframework.def.*;
import org.auraframework.impl.css.StyleTestCase;
import org.auraframework.system.AuraContext.Authentication;
import org.auraframework.system.AuraContext.Format;
import org.auraframework.system.AuraContext.Mode;
import org.auraframework.throwable.quickfix.QuickFixException;
import org.auraframework.throwable.quickfix.ThemeValueNotFoundException;
import org.auraframework.util.AuraTextUtil;
import org.auraframework.util.json.Json;
import org.auraframework.util.json.JsonReader;

import com.google.common.collect.Sets;

/**
 * Tests for StyleDefImpl.
 * 
 * @since 0.0.240
 */
public class StyleDefImplTest extends StyleTestCase {
    public StyleDefImplTest(String name) {
        super(name);
    }

    /**
     * TODONM remove
     * 
     * StyleDef must have a dependency on a NamespaceDef.
     */
    public void testAppendDependenciesHasNamespaceDef() throws Exception {
        String name = String.format("%s.someStyle", getAuraTestingUtil().getNonce(getName()));
        DefDescriptor<StyleDef> styleDesc = Aura.getDefinitionService().getDefDescriptor(name, StyleDef.class);
        addSourceAutoCleanup(styleDesc, ".THIS {}");

        DefDescriptor<NamespaceDef> namespaceDesc = Aura.getDefinitionService().getDefDescriptor(
                String.format("%s://%s", DefDescriptor.MARKUP_PREFIX, styleDesc.getNamespace()), NamespaceDef.class);
        addSourceAutoCleanup(namespaceDesc, "<aura:namespace></aura:namespace>");

        // need to restart context because old context will not have the new
        // namespace registered
        Aura.getContextService().endContext();
        Aura.getContextService().startContext(Mode.UTEST, Format.JSON, Authentication.AUTHENTICATED);

        StyleDef styleDef = styleDesc.getDef();
        Set<DefDescriptor<?>> deps = Sets.newHashSet();
        styleDef.appendDependencies(deps);

        DefDescriptor<NamespaceDef> nsDesc = Aura.getDefinitionService().getDefDescriptor(styleDesc.getNamespace(),
                NamespaceDef.class);
        assertTrue("NamespaceDef missing from StyleDef dependencies", deps.contains(nsDesc));
    }

    public void testThemeDependenciesNsThemeOnly() throws QuickFixException {
        DefDescriptor<ThemeDef> theme = addNsTheme(theme().var("color", "red"));
        DefDescriptor<StyleDef> style = addStyleDef(".THIS {color: theme(color) }");

        Set<DefDescriptor<?>> dependencies = Sets.newHashSet();
        style.getDef().appendDependencies(dependencies);
        assertTrue(dependencies.contains(theme));
    }

    public void testThemeDependenciesLocalThemeOnly() throws QuickFixException {
        DefDescriptor<StyleDef> style = addStyleDef(".THIS {color: theme(color) }");
        DefDescriptor<ThemeDef> theme = addLocalTheme(theme().var("color", "red"), style);

        assertTrue("expected theme to be a localTheme", theme.getDef().isLocalTheme());

        Set<DefDescriptor<?>> dependencies = Sets.newHashSet();
        style.getDef().appendDependencies(dependencies);
        assertTrue(dependencies.contains(theme));
    }

    public void testThemeDependenciesBothThemes() throws QuickFixException {
        DefDescriptor<ThemeDef> nsTheme = addNsTheme(theme().var("color", "red"));
        DefDescriptor<StyleDef> style = addStyleDef(".THIS {color: theme(color) }");
        DefDescriptor<ThemeDef> localTheme = addLocalTheme(theme().var("color", "red"), style);

        Set<DefDescriptor<?>> dependencies = Sets.newHashSet();
        style.getDef().appendDependencies(dependencies);
        assertTrue("expected dependencies to contain namespace theme", dependencies.contains(nsTheme));
        assertTrue("expected dependencies to contain local theme", dependencies.contains(localTheme));
    }

    public void testThemeDependenciesDoesntHaveThemeDef() throws QuickFixException {
        DefDescriptor<ThemeDef> nsTheme = addNsTheme("<aura:theme><aura:var name='color' value='red'/></aura:theme>");
        DefDescriptor<StyleDef> style = addStyleDef(".THIS {color: red }");
        DefDescriptor<ThemeDef> localTheme = addLocalTheme(theme().var("color", "red"), style);

        Set<DefDescriptor<?>> dependencies = Sets.newHashSet();
        style.getDef().appendDependencies(dependencies);
        assertFalse(dependencies.contains(nsTheme));
        assertFalse(dependencies.contains(localTheme));
    }

    public void testInvalidRef() throws QuickFixException {
        addNsTheme("<aura:theme><aura:var name='color' value='red'/></aura:theme>");
        DefDescriptor<StyleDef> style = addStyleDef(".THIS {color: theme(bam) }");

        try {
            style.getDef().validateReferences();
            fail("expected an exception");
        } catch (Exception e) {
            checkExceptionContains(e, ThemeValueNotFoundException.class, "was not found");
        }
    }

    public void testGetClassName() throws QuickFixException {
        DefDescriptor<StyleDef> style = addStyleDef(".THIS {color: red }");
        String expected = style.getNamespace() + AuraTextUtil.initCap(style.getName());
        assertEquals(expected, style.getDef().getClassName());
    }
    /**
     * Verify that if already preloaded, StyleDef doesn't include code when serialized.
     * @throws Exception
     */
    public void testDefSerializationWhenPreloaded()throws Exception{
        DefDescriptor<StyleDef> styleDesc = addStyleDef(".THIS {color: red }");
        Aura.getContextService().getCurrentContext().setPreloading(false);
        Set<DefDescriptor<?>> preloaded = Sets.newHashSet();
        preloaded.add(styleDesc);
        Aura.getContextService().getCurrentContext().setPreloadedDefinitions(preloaded);
        verifyStyleDefSerialization(styleDesc, false);
    }
    
    /**
     * Verify that if not preloaded, StyleDef includes code when serialized.
     * @throws Exception
     */
    public void testDefSerializationWhenNotPreloaded()throws Exception{
        DefDescriptor<StyleDef> styleDesc = addStyleDef(".THIS {color: green }");
        Aura.getContextService().getCurrentContext().setPreloading(false);
        Set<DefDescriptor<?>> preloaded = Sets.newHashSet();
        Aura.getContextService().getCurrentContext().setPreloadedDefinitions(preloaded);
        verifyStyleDefSerialization(styleDesc, true);
    }
    
    @SuppressWarnings("unchecked")
    private void verifyStyleDefSerialization(DefDescriptor<StyleDef> styleDesc, Boolean expectCode)throws Exception{
        String serialized = Json.serialize(styleDesc.getDef());
        Object o = new JsonReader().read(serialized);
        assertTrue(o instanceof Map);
        Map<String, Object> outerMap = (Map<String, Object>) o;
        assertEquals(styleDesc.toString(), outerMap.get("descriptor"));
        assertEquals(styleDesc.getNamespace() + AuraTextUtil.initCap(styleDesc.getName()), outerMap.get("className"));
        if(expectCode){
            assertEquals("StyleDef content not included.", styleDesc.getDef().getCode(),outerMap.get("code"));
        }else{
            assertNull("StyleDef content should not be included.", outerMap.get("code"));
        }
    }
}
