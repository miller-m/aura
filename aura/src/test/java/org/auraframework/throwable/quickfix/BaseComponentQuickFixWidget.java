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
package org.auraframework.throwable.quickfix;

import junit.framework.Assert;

import org.auraframework.def.DefDescriptor;
import org.auraframework.def.DefDescriptor.DefType;
import org.auraframework.test.WebDriverTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Widget class for QuickFixes to create new component/application bundles. This class handles verificaiton of UI
 * differences between creating a component and application bundle.
 *
 * Logic that is different between handling component and application bundles is passed to inner class
 * BaseComponentQuickFixUtil. Logic common to all Quickfixes, including adding/removing attribtues etc, is passed to
 * QuickFixUITestUtil.
 */
public class BaseComponentQuickFixWidget {
    private BaseComponentQuickFixUtil baseCmpUtil;
    WebDriverTestCase testCase;
    private final QuickFixUITestUtil testUtil;

    public BaseComponentQuickFixWidget(DefType type, WebDriverTestCase testCase) {
        switch (type) {
        case APPLICATION:
            baseCmpUtil = new ApplicationQuickFixUtil(testCase);
            break;
        case COMPONENT:
            baseCmpUtil = new ComponentQuickFixUtil(testCase);
            break;
        default:
            throw new UnsupportedOperationException("The specified defType is not supported by the test framework:"
                    + type.name());
        }
        this.testCase = testCase;
        testUtil = new QuickFixUITestUtil(testCase);
    }

    /**
     * Verify toolbar error message and click create button.
     */
    public void verifyToolbarAndClickCreate(String cmpName) {
        baseCmpUtil.verifyToolbarAndClickCreate(cmpName);
    }

    /**
     * Verify the customization options available, such as what files to include in the bundle.
     */
    public void verifyCustomizationMenu() {
        baseCmpUtil.verifyCustomizationMenu();
    }

    /**
     * Click on fix button and verify text displayed to user.
     */
    public void clickFix(Boolean expectedSuccess, String text) throws Exception {
        testUtil.clickFix(expectedSuccess, text);
    }

    /**
     * Set the css file checkbox to be selected or not.
     */
    public void selectCssCheckbox(Boolean select) {
        baseCmpUtil.selectCssCheckbox(select);
    }

    /**
     * Set the name of the component bundle before creating it.
     */
    public void setDescriptorNames(String text) {
        baseCmpUtil.setDescriptorNames(text);
    }

    /**
     * Delete the component bundle.
     */
    public void deleteFiles(DefDescriptor<?> defDescriptor) {
        testUtil.deleteFiles(defDescriptor);
    }

    private abstract class BaseComponentQuickFixUtil {
        WebDriverTestCase testCase;
        protected By createButton;

        BaseComponentQuickFixUtil(WebDriverTestCase testCase) {
            this.testCase = testCase;
        }

        /**
         * Set the css file checkbox to be selected or not.
         */
        public void selectCssCheckbox(Boolean select) {
            By css = By.cssSelector("input[name='client.css']");
            WebElement checkbox = testCase.getDriver().findElement(css);
            if ((select && !checkbox.isSelected()) || (!select && checkbox.isSelected())) {
                checkbox.click();
            }
        }

        /**
         * Set the name of the component bundle before creating it.
         */
        public void setDescriptorNames(String text) {
            By xpath = By.cssSelector("textarea[name='descriptor']");
            WebElement textBox = testCase.getDriver().findElement(xpath);
            textBox.click();
            textBox.clear();
            textBox.sendKeys(text);
        }

        /**
         * Verify the buttons you expect to see on the QuickFix screen.
         */
        public void verifyToolbarAndClickCreate(String name) {
            Assert.assertTrue("Could not locate the create button or the label on button is invalid.",
                    testCase.isElementPresent(createButton));
            testUtil.clickButtonByLocalId("createButton");
        }

        /**
         * What other parts of a Component/Application do you want to create? Verify that menu.
         */
        public void verifyCustomizationMenu() {
            // No support for controller yet
            By jsController = By
                    .cssSelector("input[name='client.controller']");
            Assert.assertTrue("Could not locate checkbox to create JS controller file.",
                    testCase.isElementPresent(jsController));

            // No support for renderer yet
            By jsRenderer = By.cssSelector("input[name='client.renderer']");

            Assert.assertTrue("Could not locate checkbox to create JS renderer file.",
                    testCase.isElementPresent(jsRenderer));

            By css = By.cssSelector("input[name='client.css']");
            Assert.assertTrue("Could not locate checkbox to create css style file.", testCase.isElementPresent(css));

            // No support for controller yet
            By javaController = By
                    .cssSelector("input[name='java.controller']");
            Assert.assertTrue("Could not locate checkbox to create java controller file.",
                    testCase.isElementPresent(javaController));

            // No support for renderer yet
            By javaRenderer = By.cssSelector("input[name='java.renderer']");
            Assert.assertTrue("Could not locate checkbox to create java renderer file.",
                    testCase.isElementPresent(javaRenderer));
        }
    }

    private class ComponentQuickFixUtil extends BaseComponentQuickFixUtil {
        ComponentQuickFixUtil(WebDriverTestCase test) {
            super(test);
            createButton = By.xpath("//button/span[text()='Create Component Definition']");
        }

        @Override
        public void verifyCustomizationMenu() {
            super.verifyCustomizationMenu();
            By app = By.cssSelector("input[name='client.cmp']");
            Assert.assertTrue("Could not locate checkbox to create component markup file.",
                    testCase.isElementPresent(app));
            // No support for provider yet
            By jsProvider = By.cssSelector("input[name='client.provider']");
            Assert.assertTrue("Could not locate checkbox to create JS provider file.",
                    testCase.isElementPresent(jsProvider));
            // No support for provider yet
            By javaProvider = By.cssSelector("input[name='java.provider']");
            Assert.assertTrue("Could not locate checkbox to create java provider file.",
                    testCase.isElementPresent(javaProvider));
        }

        @Override
        public void verifyToolbarAndClickCreate(String name) {
            super.verifyToolbarAndClickCreate(name);
            testUtil.verifyToolbarText("No COMPONENT named " + name + " found");
        }
    }

    private class ApplicationQuickFixUtil extends BaseComponentQuickFixUtil {
        ApplicationQuickFixUtil(WebDriverTestCase test) {
            super(test);
            createButton = By.xpath("//button/span[text()='Create Application Definition']");
        }

        @Override
        public void verifyCustomizationMenu() {
            super.verifyCustomizationMenu();
            By app = By.cssSelector("input[name='client.app'][type='checkbox']");
            Assert.assertTrue("Could not locate checkbox to create application markup file.",
                    testCase.isElementPresent(app));
        }

        @Override
        public void verifyToolbarAndClickCreate(String name) {
            super.verifyToolbarAndClickCreate(name);
            testUtil.verifyToolbarText("No APPLICATION named " + name + " found");
        }
    }

}
