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

import org.apache.commons.lang3.StringUtils;
import org.auraframework.Aura;
import org.auraframework.def.ApplicationDef;
import org.auraframework.def.ComponentDef;
import org.auraframework.def.ControllerDef;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.EventDef;
import org.auraframework.def.HelperDef;
import org.auraframework.def.InterfaceDef;
import org.auraframework.def.LayoutsDef;
import org.auraframework.def.NamespaceDef;
import org.auraframework.def.ProviderDef;
import org.auraframework.def.RendererDef;
import org.auraframework.def.StyleDef;
import org.auraframework.system.Source;
import org.auraframework.test.WebDriverTestCase;
import org.auraframework.test.annotation.ThreadHostileTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.google.common.base.Function;

/**
 * Tests to verify that the client gets updated when we want it to get updated.
 */
public class ClientOutOfSyncUITest extends WebDriverTestCase {

    public ClientOutOfSyncUITest(String name) {
        super(name);
    }

    @Override
    public void perBrowserSetUp() {
        super.perBrowserSetUp();
        // these tests trigger server recompilation which can take a bit of time
        auraUITestingUtil.setTimeoutInSecs(60);
    }

    private void updateStringSource(DefDescriptor<?> desc, String content) {
        Source<?> src = getSource(desc);
        src.addOrUpdate(content);
    }

    private DefDescriptor<ComponentDef> setupTriggerComponent(String attrs,
            String body) {
        DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(
                ComponentDef.class,
                String.format(
                        baseComponentTag,
                        "controller='java://org.auraframework.impl.java.controller.JavaTestController' "
                                + attrs,
                        "<button onclick='{!c.post}'>post</button>" + body));
        DefDescriptor<?> controllerDesc = Aura.getDefinitionService()
                .getDefDescriptor(cmpDesc, DefDescriptor.JAVASCRIPT_PREFIX,
                        ControllerDef.class);
        addSourceAutoCleanup(
                controllerDesc,
                "{post:function(c){var a=c.get('c.getString');a.setParams({param:'dummy'});$A.enqueueAction(a);}}");
        return cmpDesc;
    }

    /**
     * Trigger a server action and wait for the browser to begin refreshing.
     */
    private void triggerServerAction() {
        // Careful. Android doesn't like more than one statement.
        auraUITestingUtil.getRawEval("document._waitingForReload = true;");
        auraUITestingUtil.findDomElement(By.cssSelector("button")).click();
        waitForCondition("return !document._waitingForReload");
        waitForAuraFrameworkReady();
    }

    public void testGetServerRenderingAfterMarkupChange() throws Exception {
        DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(ComponentDef.class,
                String.format(baseComponentTag, "", "hi"));
        String url = getUrl(cmpDesc);
        openNoAura(url);
        assertEquals("hi", getText(By.cssSelector("body")));
        updateStringSource(cmpDesc, String.format(baseComponentTag, "", "bye"));
        // Firefox caches the response so we need to manually include a nonce to effect a reload
        openNoAura(url + "?nonce=" + System.nanoTime());
        auraUITestingUtil.waitForElementText(By.cssSelector("body"), "bye", true);
    }

    @ThreadHostileTest("NamespaceDef modification affects namespace")
    public void testGetClientRenderingAfterStyleChange() throws Exception {
        DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(ComponentDef.class,
                String.format(baseComponentTag, "", "<div id='out'>hi</div>"));
        String className = cmpDesc.getNamespace() + StringUtils.capitalize(cmpDesc.getName());
        DefDescriptor<?> styleDesc = Aura.getDefinitionService().getDefDescriptor(cmpDesc, DefDescriptor.CSS_PREFIX,
                StyleDef.class);
        addSourceAutoCleanup(styleDesc, String.format(".%s {font-style:italic;}", className));
        addSourceAutoCleanup(
                Aura.getDefinitionService().getDefDescriptor(
                        String.format("%s://%s", DefDescriptor.MARKUP_PREFIX, styleDesc.getNamespace(),
                                DefDescriptor.MARKUP_PREFIX, styleDesc.getNamespace()), NamespaceDef.class),
                "<aura:namespace/>");
        open(cmpDesc);
        assertEquals("italic",
                auraUITestingUtil.findDomElement(By.cssSelector("." + className)).getCssValue("font-style"));
        updateStringSource(styleDesc, String.format(".%s {font-style:normal;}", className));
        open(cmpDesc);
        assertEquals("normal",
                auraUITestingUtil.findDomElement(By.cssSelector("." + className)).getCssValue("font-style"));
    }

    @ThreadHostileTest("NamespaceDef modification affects namespace")
    public void testGetClientRenderingAfterNamespaceChange() throws Exception {
        DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(ComponentDef.class,
                String.format(baseComponentTag, "", "<div id='out'>hi</div>"));
        String className = cmpDesc.getNamespace() + StringUtils.capitalize(cmpDesc.getName());
        DefDescriptor<?> styleDesc = Aura.getDefinitionService().getDefDescriptor(cmpDesc, DefDescriptor.CSS_PREFIX,
                StyleDef.class);
        addSourceAutoCleanup(styleDesc, String.format(".%s {font-size:TOKEN;}", className));
        DefDescriptor<?> namespaceDesc = Aura.getDefinitionService().getDefDescriptor(
                String.format("%s://%s", DefDescriptor.MARKUP_PREFIX, cmpDesc.getNamespace()), NamespaceDef.class);
        addSourceAutoCleanup(namespaceDesc,
                "<aura:namespace><style><tokens><TOKEN>8px</TOKEN></tokens></style></aura:namespace>");
        open(cmpDesc);
        assertEquals("8px", auraUITestingUtil.findDomElement(By.cssSelector("." + className)).getCssValue("font-size"));
        updateStringSource(namespaceDesc,
                "<aura:namespace><style><tokens><TOKEN>66px</TOKEN></tokens></style></aura:namespace>");
        open(cmpDesc);
        assertEquals("66px", auraUITestingUtil.findDomElement(By.cssSelector("." + className)).getCssValue("font-size"));
    }

    public void testGetClientRenderingAfterJsControllerChange() throws Exception {
        DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(ComponentDef.class,
                String.format(baseComponentTag, "", "<div id='click' onclick='{!c.clicked}'>click</div>"));
        DefDescriptor<?> controllerDesc = Aura.getDefinitionService().getDefDescriptor(cmpDesc,
                DefDescriptor.JAVASCRIPT_PREFIX, ControllerDef.class);
        addSourceAutoCleanup(controllerDesc, "{clicked:function(){window.tempVar='inconsequential'}}");
        open(cmpDesc);
        assertNull(auraUITestingUtil.getEval("return window.tempVar;"));
        auraUITestingUtil.findDomElement(By.cssSelector("#click")).click();
        assertEquals("inconsequential", auraUITestingUtil.getEval("return window.tempVar;"));
        updateStringSource(controllerDesc, "{clicked:function(){window.tempVar='meaningful'}}");
        open(cmpDesc);
        assertNull(auraUITestingUtil.getEval("return window.tempVar;"));
        auraUITestingUtil.findDomElement(By.cssSelector("#click")).click();
        assertEquals("meaningful", auraUITestingUtil.getEval("return window.tempVar;"));
    }

    public void testGetClientRenderingAfterJsProviderChange() throws Exception {
        DefDescriptor<ComponentDef> cmpDesc = getAuraTestingUtil().createStringSourceDescriptor(null,
                ComponentDef.class);
        DefDescriptor<?> providerDesc = Aura.getDefinitionService().getDefDescriptor(cmpDesc,
                DefDescriptor.JAVASCRIPT_PREFIX, ProviderDef.class);
        addSourceAutoCleanup(cmpDesc, String.format(baseComponentTag,
                String.format("render='client' provider='%s'", providerDesc.getQualifiedName()),
                "<aura:attribute name='given' type='string' default=''/>{!v.given}"));
        addSourceAutoCleanup(providerDesc, "({provide:function(){return {attributes:{'given':'silver spoon'}};}})");
        open(cmpDesc);
        assertEquals("silver spoon", getText(By.cssSelector("body")));
        updateStringSource(providerDesc, "({provide:function(){return {attributes:{'given':'golden egg'}};}})");
        open(cmpDesc);
        assertEquals("golden egg", getText(By.cssSelector("body")));
    }

    public void testGetClientRenderingAfterJsHelperChange() throws Exception {
        DefDescriptor<?> helperDesc = addSourceAutoCleanup(HelperDef.class, "({getHelp:function(){return 'simply';}})");
        DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(
                ComponentDef.class,
                String.format(baseComponentTag,
                        String.format("render='client' helper='%s'", helperDesc.getQualifiedName()), ""));
        open(cmpDesc);
        assertEquals("simply", auraUITestingUtil.getEval("return $A.getRoot().getDef().getHelper().getHelp();"));
        updateStringSource(helperDesc, "({getHelp:function(){return 'complicated';}})");
        open(cmpDesc);
        assertEquals("complicated", auraUITestingUtil.getEval("return $A.getRoot().getDef().getHelper().getHelp();"));
    }

    public void testGetClientRenderingAfterJsRendererChange() throws Exception {
        DefDescriptor<?> rendererDesc = addSourceAutoCleanup(RendererDef.class,
                "({render:function(){return 'default';}})");
        DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(ComponentDef.class,
                String.format(baseComponentTag, String.format("renderer='%s'", rendererDesc.getQualifiedName()), ""));
        open(cmpDesc);
        assertEquals("default", getText(By.cssSelector("body")));
        updateStringSource(rendererDesc, "({render:function(){return 'custom';}})");
        open(cmpDesc);
        assertEquals("custom", getText(By.cssSelector("body")));
    }

    public void testGetClientRenderingAfterEventChange() throws Exception {
        DefDescriptor<?> eventDesc = addSourceAutoCleanup(EventDef.class,
                "<aura:event type='APPLICATION'><aura:attribute name='explode' type='String' default='pow'/></aura:event>");
        DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(
                ComponentDef.class,
                String.format(baseComponentTag, "render='client'",
                        String.format("<aura:registerevent name='end' type='%s'/>", eventDesc.getDescriptorName())));
        open(cmpDesc);
        assertEquals("pow", auraUITestingUtil.getEval(String.format(
                "return $A.getEvt('%s').getDef().getAttributeDefs().explode.defaultValue.value;",
                eventDesc.getDescriptorName())));
        updateStringSource(eventDesc,
                "<aura:event type='APPLICATION'><aura:attribute name='explode' type='String' default='kaboom'/></aura:event>");
        open(cmpDesc);
        assertEquals("kaboom", auraUITestingUtil.getEval(String.format(
                "return $A.getEvt('%s').getDef().getAttributeDefs().explode.defaultValue.value;",
                eventDesc.getDescriptorName())));
    }

    public void testGetServerRenderingAfterInterfaceChange() throws Exception {
        DefDescriptor<?> interfaceDesc = addSourceAutoCleanup(
                InterfaceDef.class,
                "<aura:interface support='GA' description=''><aura:attribute name='entrance' type='String' default='grand'/></aura:interface>");
        DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(ComponentDef.class, String.format(baseComponentTag,
                String.format("implements='%s'", interfaceDesc.getQualifiedName()), "{!v.entrance}"));
        String url = getUrl(cmpDesc);
        openNoAura(url);
        assertEquals("grand", getText(By.cssSelector("body")));
        updateStringSource(
                interfaceDesc,
                "<aura:interface support='GA' description=''><aura:attribute name='entrance' type='String' default='secret'/></aura:interface>");
        // Firefox caches the response so we need to manually include a nonce to effect a reload
        openNoAura(url + "?nonce=" + System.nanoTime());
        auraUITestingUtil.waitForElementText(By.cssSelector("body"), "secret", true);
    }

    @ThreadHostileTest("LayoutsDef modification affects namespace")
    public void testGetClientRenderingAfterLayoutChange() throws Exception {
        DefDescriptor<ApplicationDef> appDesc = addSourceAutoCleanup(ApplicationDef.class,
                String.format(baseApplicationTag, "", "<div aura:id='xspot'/>"));
        DefDescriptor<?> layoutsDesc = Aura.getDefinitionService().getDefDescriptor(appDesc,
                DefDescriptor.MARKUP_PREFIX, LayoutsDef.class);
        addSourceAutoCleanup(
                layoutsDesc,
                "<aura:layouts default='def'><aura:layout name='def'><aura:layoutItem container='xspot'>first</aura:layoutItem></aura:layout></aura:layouts>");
        open(appDesc);
        auraUITestingUtil.waitForElementText(By.cssSelector("body"), "first", true);
        updateStringSource(
                layoutsDesc,
                "<aura:layouts default='def'><aura:layout name='def'><aura:layoutItem container='xspot'>second</aura:layoutItem></aura:layout></aura:layouts>");
        open(appDesc);
        auraUITestingUtil.waitForElementText(By.cssSelector("body"), "second", true);
    }

    public void testGetClientRenderingAfterDependencyChange() throws Exception {
        DefDescriptor<?> depDesc = addSourceAutoCleanup(ComponentDef.class,
                String.format(baseComponentTag, "", "<aura:attribute name='val' type='String' default='initial'/>"));
        DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(
                ComponentDef.class,
                String.format(baseComponentTag, "render='client'",
                        String.format("<aura:dependency resource='%s'/>", depDesc.getQualifiedName())));
        open(cmpDesc);
        assertEquals("initial", auraUITestingUtil.getEval(String.format(
                "return $A.componentService.newComponent('%s').get('v.val');", depDesc.getDescriptorName())));
        updateStringSource(depDesc,
                String.format(baseComponentTag, "", "<aura:attribute name='val' type='String' default='final'/>"));
        open(cmpDesc);
        assertEquals("final", auraUITestingUtil.getEval(String.format(
                "return $A.componentService.newComponent('%s').get('v.val');", depDesc.getDescriptorName())));
    }

    public void testPostAfterMarkupChange() throws Exception {
        DefDescriptor<ComponentDef> cmpDesc = setupTriggerComponent("", "<div id='sample'>free</div>");
        open(cmpDesc);
        assertEquals("free", getText(By.cssSelector("#sample")));
        updateStringSource(cmpDesc, String.format(baseComponentTag,
                "controller='java://org.auraframework.impl.java.controller.JavaTestController'",
                "<button onclick='{!c.post}'>post</button><div id='sample'>deposit</div>"));
        triggerServerAction();
        auraUITestingUtil.waitForElementText(By.cssSelector("#sample"), "deposit", true);
    }

    @ThreadHostileTest("NamespaceDef modification affects namespace")
    public void testPostAfterStyleChange() throws Exception {
        DefDescriptor<ComponentDef> cmpDesc = setupTriggerComponent("", "<div id='out'>hi</div>");
        String className = cmpDesc.getNamespace() + StringUtils.capitalize(cmpDesc.getName());
        DefDescriptor<?> styleDesc = Aura.getDefinitionService().getDefDescriptor(cmpDesc, DefDescriptor.CSS_PREFIX,
                StyleDef.class);
        addSourceAutoCleanup(styleDesc, String.format(".%s {font-style:italic;}", className));
        addSourceAutoCleanup(
                Aura.getDefinitionService().getDefDescriptor(
                        String.format("%s://%s", DefDescriptor.MARKUP_PREFIX, styleDesc.getNamespace(),
                                DefDescriptor.MARKUP_PREFIX, styleDesc.getNamespace()), NamespaceDef.class),
                "<aura:namespace/>");
        open(cmpDesc);
        assertEquals("italic",
                auraUITestingUtil.findDomElement(By.cssSelector("." + className)).getCssValue("font-style"));
        updateStringSource(styleDesc, String.format(".%s {font-style:normal;}", className));
        triggerServerAction();
        auraUITestingUtil.waitForElementFunction(By.cssSelector("." + className), new Function<WebElement, Boolean>() {
            @Override
            public Boolean apply(WebElement element) {
                return "normal".equals(element.getCssValue("font-style"));
            }
        });
    }

    /**
     * A routine to do _many_ iterations of a client out of sync test.
     * 
     * This test really shouldn't be run unless one of the tests is flapping. It lets you iterate a number of times to
     * force a failure.... No guarantees, but without the _waitingForReload check in the trigger function, this will
     * cause a failure in very few iterations.
     */
    @ThreadHostileTest("NamespaceDef modification affects namespace")
    public void _testPostManyAfterStyleChange() throws Exception {
        DefDescriptor<ComponentDef> cmpDesc = setupTriggerComponent("", "<div id='out'>hi</div>");
        String className = cmpDesc.getNamespace() + StringUtils.capitalize(cmpDesc.getName());
        DefDescriptor<?> styleDesc = Aura.getDefinitionService().getDefDescriptor(cmpDesc, DefDescriptor.CSS_PREFIX,
                StyleDef.class);
        addSourceAutoCleanup(styleDesc, String.format(".%s {font-style:italic;}", className));
        addSourceAutoCleanup(
                Aura.getDefinitionService().getDefDescriptor(
                        String.format("%s://%s", DefDescriptor.MARKUP_PREFIX, styleDesc.getNamespace(),
                                DefDescriptor.MARKUP_PREFIX, styleDesc.getNamespace()), NamespaceDef.class),
                "<aura:namespace/>");
        open(cmpDesc);
        assertEquals("italic",
                auraUITestingUtil.findDomElement(By.cssSelector("." + className)).getCssValue("font-style"));
        for (int i = 0; i < 1000; i++) {
            updateStringSource(styleDesc, String.format(".%s {font-style:normal;}", className));
            triggerServerAction();
            auraUITestingUtil.waitForElementFunction(By.cssSelector("." + className),
                    new Function<WebElement, Boolean>() {
                        @Override
                        public Boolean apply(WebElement element) {
                            return "normal".equals(element.getCssValue("font-style"));
                        }
                    });
            updateStringSource(styleDesc, String.format(".%s {font-style:italic;}", className));
            triggerServerAction();
            auraUITestingUtil.waitForElementFunction(By.cssSelector("." + className),
                    new Function<WebElement, Boolean>() {
                        @Override
                        public Boolean apply(WebElement element) {
                            return "italic".equals(element.getCssValue("font-style"));
                        }
                    });
        }
    }

    @ThreadHostileTest("NamespaceDef modification affects namespace")
    public void testPostAfterNamespaceChange() throws Exception {
        DefDescriptor<ComponentDef> cmpDesc = setupTriggerComponent("", "<div id='out'>hi</div>");
        String className = cmpDesc.getNamespace() + StringUtils.capitalize(cmpDesc.getName());
        DefDescriptor<?> styleDesc = Aura.getDefinitionService().getDefDescriptor(cmpDesc, DefDescriptor.CSS_PREFIX,
                StyleDef.class);
        addSourceAutoCleanup(styleDesc, String.format(".%s {font-size:TOKEN;}", className));
        DefDescriptor<?> namespaceDesc = Aura.getDefinitionService().getDefDescriptor(
                String.format("%s://%s", DefDescriptor.MARKUP_PREFIX, cmpDesc.getNamespace()), NamespaceDef.class);
        addSourceAutoCleanup(namespaceDesc,
                "<aura:namespace><style><tokens><TOKEN>8px</TOKEN></tokens></style></aura:namespace>");
        open(cmpDesc);
        assertEquals("8px", auraUITestingUtil.findDomElement(By.cssSelector("." + className)).getCssValue("font-size"));
        updateStringSource(namespaceDesc,
                "<aura:namespace><style><tokens><TOKEN>66px</TOKEN></tokens></style></aura:namespace>");
        triggerServerAction();
        auraUITestingUtil.waitForElementFunction(By.cssSelector("." + className), new Function<WebElement, Boolean>() {
            @Override
            public Boolean apply(WebElement element) {
                return "66px".equals(element.getCssValue("font-size"));
            }
        });
    }

    public void testPostAfterJsControllerChange() throws Exception {
        DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(
                ComponentDef.class,
                String.format(
                        baseComponentTag,
                        "controller='java://org.auraframework.impl.java.controller.JavaTestController'",
                        "<button onclick='{!c.post}'>post</button><div id='click' onclick='{!c.clicked}'>click</div>"));
        DefDescriptor<?> controllerDesc = Aura.getDefinitionService()
                .getDefDescriptor(cmpDesc, DefDescriptor.JAVASCRIPT_PREFIX,
                        ControllerDef.class);
        addSourceAutoCleanup(
                controllerDesc,
                "{post:function(c){var a=c.get('c.getString');a.setParams({param:'dummy'});$A.enqueueAction(a);},clicked:function(){window.tempVar='inconsequential'}}");
        open(cmpDesc);
        assertNull(auraUITestingUtil.getEval("return window.tempVar;"));
        auraUITestingUtil.findDomElement(By.cssSelector("#click")).click();
        assertEquals("inconsequential",
                auraUITestingUtil.getEval("return window.tempVar;"));
        updateStringSource(
                controllerDesc,
                "{post:function(c){var a=c.get('c.getString');a.setParams({param:'dummy'});$A.enqueueAction(a);},clicked:function(){window.tempVar='meaningful'}}");
        triggerServerAction();
        // wait for page to reload by checking that our tempVar is undefined
        // again
        auraUITestingUtil.waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return (Boolean) auraUITestingUtil
                        .getEval("return !window.tempVar;");
            }
        });
        auraUITestingUtil.findDomElement(By.cssSelector("#click")).click();
        auraUITestingUtil.waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return "meaningful".equals(auraUITestingUtil
                        .getEval("return window.tempVar;"));
            }
        });
    }

    public void testPostAfterJsProviderChange() throws Exception {
        DefDescriptor<ComponentDef> cmpDesc = getAuraTestingUtil()
                .createStringSourceDescriptor(null, ComponentDef.class);
        DefDescriptor<?> providerDesc = Aura.getDefinitionService()
                .getDefDescriptor(cmpDesc, DefDescriptor.JAVASCRIPT_PREFIX,
                        ProviderDef.class);
        addSourceAutoCleanup(
                cmpDesc,
                String.format(
                        baseComponentTag,
                        String.format(
                                "controller='java://org.auraframework.impl.java.controller.JavaTestController' provider='%s'",
                                providerDesc.getQualifiedName()),
                        "<button onclick='{!c.post}'>post</button><aura:attribute name='given' type='string' default=''/><div id='result'>{!v.given}</div>"));
        DefDescriptor<?> controllerDesc = Aura.getDefinitionService()
                .getDefDescriptor(cmpDesc, DefDescriptor.JAVASCRIPT_PREFIX,
                        ControllerDef.class);
        addSourceAutoCleanup(
                controllerDesc,
                "{post:function(c){var a=c.get('c.getString');a.setParams({param:'dummy'});$A.enqueueAction(a);}}");
        addSourceAutoCleanup(providerDesc,
                "({provide:function(){return {attributes:{'given':'silver spoon'}};}})");
        open(cmpDesc);
        assertEquals("silver spoon", getText(By.cssSelector("#result")));
        updateStringSource(providerDesc,
                "({provide:function(){return {attributes:{'given':'golden egg'}};}})");
        triggerServerAction();
        auraUITestingUtil.waitForElementText(By.cssSelector("#result"),
                "golden egg", true);
    }

    public void testPostAfterJsHelperChange() throws Exception {
        DefDescriptor<?> helperDesc = addSourceAutoCleanup(HelperDef.class, "({getHelp:function(){return 'simply';}})");
        DefDescriptor<ComponentDef> cmpDesc = setupTriggerComponent(
                String.format("helper='%s'", helperDesc.getQualifiedName()), "");
        open(cmpDesc);
        assertEquals("simply", auraUITestingUtil.getEval("return $A.getRoot().getDef().getHelper().getHelp();"));
        updateStringSource(helperDesc, "({getHelp:function(){return 'complicated';}})");
        triggerServerAction();
        auraUITestingUtil.waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                auraUITestingUtil.waitForDocumentReady();
                auraUITestingUtil.waitForAuraFrameworkReady(null);
                return "complicated".equals(auraUITestingUtil
                        .getEval("return window.$A && $A.getRoot() && $A.getRoot().getDef().getHelper().getHelp();"));
            }
        });
    }

    public void testPostAfterJsRendererChange() throws Exception {
        DefDescriptor<?> rendererDesc = addSourceAutoCleanup(
                RendererDef.class,
                "({render:function(){var e=document.createElement('div');e.id='target';e.appendChild(document.createTextNode('default'));var r=this.superRender();r.push(e);return r;}})");
        DefDescriptor<ComponentDef> cmpDesc = setupTriggerComponent(
                String.format("renderer='%s'", rendererDesc.getQualifiedName()), "");
        open(cmpDesc);
        assertEquals("default", getText(By.cssSelector("#target")));
        updateStringSource(
                rendererDesc,
                "({render:function(){var e=document.createElement('div');e.id='target';e.appendChild(document.createTextNode('custom'));var r=this.superRender();r.push(e);return r;}})");
        triggerServerAction();
        auraUITestingUtil.waitForElementText(By.cssSelector("#target"), "custom", true);
    }

    public void testPostAfterEventChange() throws Exception {
        final DefDescriptor<?> eventDesc = addSourceAutoCleanup(EventDef.class,
                "<aura:event type='APPLICATION'><aura:attribute name='explode' type='String' default='pow'/></aura:event>");
        DefDescriptor<ComponentDef> cmpDesc = setupTriggerComponent("",
                String.format("<aura:registerevent name='end' type='%s'/>", eventDesc.getDescriptorName()));
        open(cmpDesc);
        assertEquals("pow", auraUITestingUtil.getEval(String.format(
                "return $A.getEvt('%s').getDef().getAttributeDefs().explode.defaultValue.value;",
                eventDesc.getDescriptorName())));
        updateStringSource(eventDesc,
                "<aura:event type='APPLICATION'><aura:attribute name='explode' type='String' default='kaboom'/></aura:event>");
        triggerServerAction();
        auraUITestingUtil.waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                auraUITestingUtil.waitForDocumentReady();
                auraUITestingUtil.waitForAuraFrameworkReady(null);
                String eval = String
                        .format("return ((window.$A && $A.getEvt('%s')) && (window.$A && $A.getEvt('%s')).getDef().getAttributeDefs().explode.defaultValue.value);",
                                eventDesc.getDescriptorName(), eventDesc.getDescriptorName());
                return "kaboom".equals(auraUITestingUtil.getEval(eval));
            }
        });
    }

    public void testPostAfterInterfaceChange() throws Exception {
        DefDescriptor<?> interfaceDesc = addSourceAutoCleanup(
                InterfaceDef.class,
                "<aura:interface support='GA' description=''><aura:attribute name='entrance' type='String' default='grand'/></aura:interface>");
        DefDescriptor<ComponentDef> cmpDesc = setupTriggerComponent(
                String.format("implements='%s'", interfaceDesc.getQualifiedName()),
                "<div id='target'>{!v.entrance}</div>");
        open(cmpDesc);
        assertEquals("grand", getText(By.cssSelector("#target")));
        updateStringSource(
                interfaceDesc,
                "<aura:interface support='GA' description=''><aura:attribute name='entrance' type='String' default='secret'/></aura:interface>");
        triggerServerAction();
        auraUITestingUtil.waitForElementText(By.cssSelector("#target"), "secret", true);
    }

    public void testPostAfterDependencyChange() throws Exception {
        final DefDescriptor<?> depDesc = addSourceAutoCleanup(ComponentDef.class,
                String.format(baseComponentTag, "", "<aura:attribute name='val' type='String' default='initial'/>"));
        DefDescriptor<ComponentDef> cmpDesc = setupTriggerComponent("",
                String.format("<aura:dependency resource='%s'/>", depDesc.getQualifiedName()));
        open(cmpDesc);
        assertEquals("initial", auraUITestingUtil.getEval(String.format(
                "return $A.componentService.getDef('%s').getAttributeDefs().getDef('val').getDefault();",
                depDesc.getDescriptorName())));
        updateStringSource(depDesc,
                String.format(baseComponentTag, "", "<aura:attribute name='val' type='String' default='final'/>"));
        triggerServerAction();
        auraUITestingUtil.waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                auraUITestingUtil.waitForDocumentReady();
                auraUITestingUtil.waitForAuraFrameworkReady(null);
                return "final".equals(auraUITestingUtil.getEval(String
                        .format("return window.$A && $A.componentService.getDef('%s').getAttributeDefs().getDef('val').getDefault();",
                                depDesc.getDescriptorName())));
            }
        });
    }
}
