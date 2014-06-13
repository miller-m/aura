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
package configuration;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.auraframework.test.PooledRemoteWebDriverFactory;
import org.auraframework.test.RemoteWebDriverFactory;
import org.auraframework.test.SauceUtil;
import org.auraframework.test.SeleniumServerLauncher;
import org.auraframework.test.WebDriverProvider;
import org.auraframework.test.configuration.JettyTestServletConfig;
import org.auraframework.test.configuration.TestServletConfig;
import org.auraframework.util.ServiceLoaderImpl.AuraConfiguration;
import org.auraframework.util.ServiceLoaderImpl.Impl;
import org.openqa.selenium.net.PortProber;

/**
 */
@AuraConfiguration
public class AuraIntegrationTestConfig {
    @Impl
    public static TestServletConfig auraJettyServletTestInfo() throws Exception {
        for(StackTraceElement item: Thread.currentThread().getStackTrace()) {
            System.out.println("here is a class name");
            System.out.println(item.getClassName());
            System.out.println("here is a method name");
            System.out.println(item.getMethodName());
            System.out.println("here is a file name");
            System.out.println(item.getFileName());
        }
        return new JettyTestServletConfig();
    }

    @Impl
    public static WebDriverProvider auraWebDriverProvider() throws Exception {
        URL serverUrl;
        boolean runningOnSauceLabs = SauceUtil.areTestsRunningOnSauce();
        try {
            String hubUrlString = System.getProperty(WebDriverProvider.WEBDRIVER_SERVER_PROPERTY);
            if ((hubUrlString != null) && !hubUrlString.equals("")) {
                if (runningOnSauceLabs) {
                    serverUrl = SauceUtil.getSauceServerUrl();
                } else {
                    serverUrl = new URL(hubUrlString);
                }
            } else {

                int serverPort = PortProber.findFreePort();

                // quiet the verbose grid logging
                Logger selLog = Logger.getLogger("org.openqa");
                selLog.setLevel(Level.SEVERE);

                SeleniumServerLauncher.start("localhost", serverPort);
                serverUrl = new URL(String.format("http://localhost:%s/wd/hub", serverPort));
                System.setProperty(WebDriverProvider.WEBDRIVER_SERVER_PROPERTY, serverUrl.toString());
            }
            Logger.getLogger(AuraIntegrationTestConfig.class.getName()).info("Selenium server url: " + serverUrl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
        if (!runningOnSauceLabs && Boolean.parseBoolean(System.getProperty(WebDriverProvider.REUSE_BROWSER_PROPERTY))) {
            return new PooledRemoteWebDriverFactory(serverUrl);
        } else {
            return new RemoteWebDriverFactory(serverUrl);
        }
    }
}
