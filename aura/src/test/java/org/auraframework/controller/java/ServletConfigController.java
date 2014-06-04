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
package org.auraframework.controller.java;

import org.auraframework.Aura;
import org.auraframework.http.AuraBaseServlet;
import org.auraframework.system.Annotations.Key;
import org.auraframework.test.AuraTestCase;

/**
 * Let tests adjust servlet configuration.
 * 
 * @since 0.0.178
 */
public class ServletConfigController {
    /**
     * Set the servlet production mode configuration. Don't forget to restore config after test.
     * 
     * @param isProduction true/false
     */
    public static void setProductionConfig(@Key("isProduction") boolean isProduction) {
        AuraTestCase.getMockConfigAdapter().setIsProduction(isProduction);
        System.out.println("PROD : " + isProduction + " , " + Aura.getConfigAdapter().isProduction() + " - "
                + Aura.getConfigAdapter());
    }

    /**
     * Set the servlet isJar configuration. Don't forget to restore config after test.
     * 
     * @param isAuraJSStatic true/false
     */
    public static void setIsAuraJSStatic(@Key("isAuraJSStatic") boolean isAuraJSStatic) {
        AuraTestCase.getMockConfigAdapter().setIsAuraJSStatic(isAuraJSStatic);
    }

    /**
     * Set the servlet application cache configuration. Don't forget to restore config after test.
     * 
     * @param isDisabled true/false
     */
    public static void setAppCacheDisabled(@Key("isDisabled") Boolean isDisabled) {
        AuraTestCase.getMockConfigAdapter().setIsClientAppcacheEnabled(!isDisabled);
    }

    /**
     * Get the servlet's current last modification timestamp.
     */
    public static long getLastMod() throws Exception {
        return AuraBaseServlet.getLastMod();
    }

    public static long getBuildTimestamp() throws Exception {
        return Aura.getConfigAdapter().getBuildTimestamp();
    }

    public static void main(String[] args) {
        setProductionConfig(true);
    }
}
