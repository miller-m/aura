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
package org.auraframework.components.ui.button;

import org.auraframework.test.WebDriverTestCase;
import org.auraframework.util.test.perf.PerfTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

@PerfTest
public class ButtonUITest extends WebDriverTestCase {

    public ButtonUITest(String name) {
        super(name);
    }

    public void testButtonLabelRequired() throws Exception {
        final String errorMsg = "COMPONENT markup://uitest:button_LabelRequiredTest is missing required attribute 'label'";
        openNoAura("/uitest/button_LabelRequiredTest.cmp");
        auraUITestingUtil.waitForDocumentReady();
        WebDriverWait wait = new WebDriverWait(getDriver(), timeoutInSecs);
        wait.withMessage("Required label error not displayed");
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return auraUITestingUtil.getQuickFixMessage().contains(errorMsg);
            }
        });
    }

    public void testDomEventAttributeOnPressEvent() throws Exception {
        open("/uitest/button_Test.cmp");
        WebElement btn = findDomElement(By.cssSelector(".testDomEventBtn"));
        btn.click();

        String valueExpression = auraUITestingUtil.getValueFromRootExpr("v.isDomEventSet");
        valueExpression = auraUITestingUtil.prepareReturnStatement(valueExpression);
        assertTrue("domEvent attribute on event should have been set",
                auraUITestingUtil.getBooleanEval(valueExpression));
    }
}