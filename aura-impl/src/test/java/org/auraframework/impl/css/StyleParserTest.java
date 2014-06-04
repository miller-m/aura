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
package org.auraframework.impl.css;

import org.auraframework.def.DefDescriptor;
import org.auraframework.def.StyleDef;
import org.auraframework.impl.AuraImplTestCase;
import org.auraframework.impl.css.parser.CSSParser;
import org.auraframework.impl.css.parser.StyleParser;
import org.auraframework.impl.system.DefDescriptorImpl;
import org.auraframework.system.Client;
import org.auraframework.throwable.quickfix.StyleParserException;

/**
 * This class tests the CSS validation in place for Aura Components. Aura components can have a .css file specified in
 * the same directory. The first part of every selector has to be the name of the component, including the namespace.
 * For example, if there is a component called aura:test, then every selector in the test.css file has to being with
 * .auraTest
 */
public class StyleParserTest extends AuraImplTestCase {
    public StyleParserTest(String name) {
        super(name);
    }

    private void serializeAndGoldFile(StyleDef styleDef, String suffix) throws Exception {
        String styleStr = toJson(styleDef);
        // strip any cachebusters from URLs (simplistically), we have other
        // tests for that
        styleStr = styleStr.replaceAll("[\\?&]aura.cb=\\d+", "");
        // strip quotes from URLs
        styleStr = styleStr.replaceAll("url\\s*\\(\\s*['\"]?([^'\"]+)['\"]?\\)", "url($1)");
        goldFileJson(styleStr, suffix);
    }

    /**
     * Tests for validation of css files and creation of StyleDefs for aura components. {@link StyleParser}
     *
     * Negative Test Case 1: Have two css files for a single component.
     *
     * Expected result: Pick up only the style defined in file named exactly as the component but with a .css file type.
     */
    public void testTwoCssFilesForAComponent() throws Exception {
        DefDescriptor<StyleDef> descriptor = DefDescriptorImpl.getInstance("test.testTwoCSSFiles", StyleDef.class);
        StyleDef style = descriptor.getDef();
        assertTrue(style.getName().equals("testTwoCSSFiles"));
        serializeAndGoldFile(style, "_styleDef");
    }

    /**
     * Tests for validation of css files and creation of StyleDefs for aura components. {@link StyleParser}
     *
     * Negative Test Case 2: Have a single css file for a component. Break the Case sensitivity rule of CSS selectors.
     * This test is sufficient to cover the test case for missing prefix for selectors.
     *
     * Expected result: The parser should throw a StyleParserException.
     */
    public void testCaseSensitivity() throws Exception {
        DefDescriptor<StyleDef> descriptor = DefDescriptorImpl.getInstance("test.testStyleSelectorCaseSensitivity",
                StyleDef.class);
        try {
            descriptor.getDef();
            fail("Should have caught the css selector in caps.");
        } catch (StyleParserException expected) {
            assertTrue(expected.getMessage().contains(CSSParser.ISSUE_MESSAGE));
        }
    }

    /**
     * Tests for validation of css files and creation of StyleDefs for aura components. {@link StyleParser}
     *
     * Positive Test Case 1: Have a single valid css file for a component.
     *
     * Expected result: Pick up only the style defined in file.
     */
    public void testValidCss() throws Exception {
        DefDescriptor<StyleDef> descriptor = DefDescriptorImpl.getInstance("test.testValidCSS", StyleDef.class);
        StyleDef style = descriptor.getDef();
        assertTrue(style.getName().equals("testValidCSS"));
        serializeAndGoldFile(style, "_styleDef");
    }

    /**
     * Tests for validation of css files and creation of StyleDefs for aura components. {@link StyleParser}
     *
     * Positive Test Case 2: Have an invalid css file for a single component. Just leave on of the selectors definitions
     * open.
     *
     * Expected result: The parser should throw a StyleParserException.
     */
    public void testInvalidCss() throws Exception {
        DefDescriptor<StyleDef> descriptor = DefDescriptorImpl.getInstance("test.testInValidCSS", StyleDef.class);
        try {
            descriptor.getDef();
            fail("Should have caught the bad css");
        } catch (StyleParserException expected) {
            assertTrue("Incorrect message in StyleParserException",
                    expected.getMessage().contains("Parse error in testTestInValidCSS at line 40 column 1"));
        }
    }

    /**
     * Tests for validation of css files and creation of StyleDefs for aura components. {@link StyleParser}
     *
     * Positive Test Case 3: Nested Component, have valid css file for parent component and an invalid file for child
     * component.
     *
     * Expected result: The parser just considers the css file for the parent for validation.
     */
    public void testValidNestedComponents() throws Exception {
        DefDescriptor<StyleDef> descriptor = DefDescriptorImpl.getInstance("test.testStyleValidParent", StyleDef.class);
        StyleDef style = descriptor.getDef();
        assertTrue(style.getName().equals("testStyleValidParent"));
        serializeAndGoldFile(style, "_styleDef");
    }

    /**
     * Tests for substitution of the THIS token with the component's namespace. {@link StyleParser}
     *
     * Expected result: Css file is valid.
     */
    public void testStyleNamespaceToken() throws Exception {
        DefDescriptor<StyleDef> descriptor = DefDescriptorImpl.getInstance("test.testStyleNamespaceToken",
                StyleDef.class);
        StyleDef style = descriptor.getDef();
        assertTrue(style.getName().equals("testStyleNamespaceToken"));
        serializeAndGoldFile(style, "_styleDef");
    }

    /**
     * Tests for substitution of the THIS token with the component's namespace for a set of valid CSS rules.
     * {@link StyleParser}
     *
     * Expected result: The parser just considers the css file for the parent for validation.
     */
    public void testStyleNamespaceTokenValidCss() throws Exception {
        DefDescriptor<StyleDef> descriptor = DefDescriptorImpl.getInstance("test.testStyleNamespaceTokenValidCSS",
                StyleDef.class);
        StyleDef style = descriptor.getDef();
        assertTrue(style.getName().equals("testStyleNamespaceTokenValidCSS"));
        serializeAndGoldFile(style, "_styleDef");
    }

    /**
     * Tests for substitution of the THIS token with the component's namespace for a set of invalid CSS rules.
     * {@link StyleParser}
     *
     * Expected result: The parser will throw exception based on type of error it encounters while parsing the CSS file.
     */
    public void testStyleNamespaceTokenInvalidCSS() throws Exception {
        DefDescriptor<StyleDef> descriptor = DefDescriptorImpl.getInstance("test.testStyleNamespaceTokenInvalidCSS",
                StyleDef.class);
        try {
            descriptor.getDef();
            fail("Exception not thrown for some set of invalid CSS rules!");
        } catch (StyleParserException expected) {
            assertTrue("Incorrect message in StyleParserException", expected.getMessage().contains(
                    "Parse error in testTestStyleNamespaceTokenInvalidCSS at line 76 column 11"));
        }
    }

    /**
     * Tests for browser conditionals in the CSS file. Tests if the rules for client (browser) type "chrome" get
     * rendered.
     */
    public void testStyleNamespaceTrueConditions() throws Exception {
        DefDescriptor<StyleDef> descriptor = DefDescriptorImpl.getInstance("test.testStyleNamespaceTrueConditions",
                StyleDef.class);

        Client client = new Client("chrome");
        StyleDef style = descriptor.getDef();
        assertTrue(style.getName().equals("testStyleNamespaceTrueConditions"));
        goldFileText(style.getCode(client.getType()));
    }

    /**
     * Tests for invalid values as part of browser conditionals.
     */
    public void testStyleNamespaceExceptions() throws Exception {
        DefDescriptor<StyleDef> descriptor = DefDescriptorImpl.getInstance("test.testStyleNamespaceInvalidConditions",
                StyleDef.class);
        try {
            descriptor.getDef();
            fail("Exception not thrown for some set of invalid CSS rules!");
        } catch (StyleParserException expected) {
            assertTrue("Incorrect message in StyleParserException", expected.getMessage().contains(
                    "Parse error in testTestStyleNamespaceInvalidConditions at line 55 column 1"));
        }
    }

    /**
     * Tests that templateCss is not validated but standard CSS files are.
     *
     * W-1366145
     */
    public void testTemplateCssValid() throws Exception {
        DefDescriptor<StyleDef> templateCssDesc = DefDescriptorImpl.getInstance("templateCss://test.testTemplateCss",
                StyleDef.class);
        DefDescriptor<StyleDef> cssDesc = DefDescriptorImpl.getInstance("css://test.testTemplateCss", StyleDef.class);

        // templateCss should work here since not validated
        try {
            templateCssDesc.getDef();
        } catch (Exception e) {
            fail("CSS should be valid for templateCss, no Exception should be thrown.");
        }

        // CSS should fail on validation
        try {
            cssDesc.getDef();
            fail("Parser should have thrown StyleParserException trying to parse invalid CSS.");
        } catch (StyleParserException e) {
            assertTrue("Incorrect message in StyleParserException", e
                    .getMessage()
                    .toString()
                    .startsWith(
                            "Issue(s) found by Parser:CSS selectors must include "
                                    + "component class: \"testTestTemplateCss\" in testTestTemplateCss"));
        }
    }
}
