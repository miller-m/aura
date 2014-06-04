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
package org.auraframework.http;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.auraframework.controller.java.ServletConfigController;
import org.auraframework.def.ApplicationDef;
import org.auraframework.def.DefDescriptor;
import org.auraframework.test.AuraHttpTestCase;
import org.auraframework.test.annotation.ThreadHostileTest;
import org.auraframework.test.annotation.UnAdaptableTest;
import org.auraframework.test.client.UserAgent;
import org.auraframework.util.AuraTextUtil;

import com.google.common.collect.Lists;
import com.google.common.io.LineReader;

public class AppCacheManifestHttpTest extends AuraHttpTestCase {

    private static final String APPCACHE_SUPPORTED_USERAGENT = UserAgent.GOOGLE_CHROME.getUserAgentString();
    private static final String APPCACHE_UNSUPPORTED_USERAGENT = UserAgent.EMPTY.getUserAgentString();
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<html data-lm=\"(.*?)\" manifest=\"(.*?)\">");

    private class ManifestInfo {
        String url;
        String lastmod;

        ManifestInfo(String url, String lastmod) {
            this.url = url;
            this.lastmod = lastmod;
        }
    }

    public AppCacheManifestHttpTest(String name) {
        super(name);
    }

    private ManifestInfo getManifestInfo(String appPath) throws Exception {
        HttpGet get = obtainGetMethod(appPath + "?aura.mode=PROD");
        HttpResponse response = perform(get);
        String responseBody = getResponseBody(response);
        get.releaseConnection();
        Matcher m = HTML_TAG_PATTERN.matcher(responseBody);
        String url = null;
        String lastmod = null;
        if (m.find()) {
            lastmod = m.group(1);
            url = m.group(2);
        }
        return new ManifestInfo(url, lastmod);
    }

    private String getManifestErrorUrl(String manifestURI) {
        return manifestURI + "?aura.error=true";
    }

    private List<String> getManifestLinks(String manifestContents) throws IOException {
        List<String> links = Lists.newArrayList();

        LineReader reader = new LineReader(new StringReader(manifestContents));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.startsWith("/")) {
                links.add(line);
            }
        }
        return links;
    }

    private List<String> getRequiredLinks() throws Exception {
        List<String> required = Lists.newArrayList();
        required.add(".*/aura_prod\\.js\\?aura.fwuid=.*");
        return required;
    }

    private void assertManifest(String manifestContent, List<String> requiredLinks, String lastMod) throws Exception {
        assertManifestFormat(manifestContent);
        assertManifestLastMod(manifestContent, lastMod);

        List<String> links = getManifestLinks(manifestContent);
        List<String> required = getRequiredLinks();
        required.addAll(requiredLinks);
        assertRequiredLinks(required, links);
        assertLinksReachable(links);
    }

    private void assertManifestFormat(String manifestContent) {
        if (!manifestContent.startsWith("CACHE MANIFEST\n")) {
            fail("Manifest should starts with: " + "CACHE MANIFEST");
        }
    }

    /**
     * Check for the 'last mod' line.
     * 
     * Note that since the reformat of this line, there is no real last mod date in the manifest.
     */
    private void assertManifestLastMod(String manifestContent, String lastMod) throws Exception {
        String lastModMarker = String.format("\n# LAST MOD: app=");
        if (!manifestContent.contains(lastModMarker)) {
            fail("Line not found: " + lastModMarker);
        }
    }

    private void assertRequiredLinks(List<String> required, List<String> links) throws Exception {
        for (String requiredLink : required) {
            boolean foundFlag = false;
            for (String link : links) {
                if (link.matches(requiredLink)) {
                    foundFlag = true;
                }
            }
            if (!foundFlag) {
                fail("Missing required link: " + requiredLink + " but got instead: " + links);
            }
        }
    }

    private void assertLinksReachable(List<String> links) throws Exception {
        for (String link : links) {
            HttpGet get = obtainGetMethod(link);
            HttpResponse response = perform(get);
            int statusCode = getStatusCode(response);
            get.releaseConnection();
            assertEquals(HttpStatus.SC_OK, statusCode);
        }
    }

    private void assertManifestHeaders(HttpResponse response) throws Exception {
        String cacheControlHeader = String.format(",%s,", response.getFirstHeader(HttpHeaders.CACHE_CONTROL).getValue()
                .replaceAll("\\s", ""));
        if (!cacheControlHeader.contains(",no-cache,") || !cacheControlHeader.contains(",no-store,")) {
            fail("Missing cache control header values in: " + cacheControlHeader);
        }

        String expectedContentType = String.format("%s;charset=%s", AuraBaseServlet.MANIFEST_CONTENT_TYPE,
                AuraBaseServlet.UTF_ENCODING);
        Header[] contentTypeHeaders = response.getHeaders(HttpHeaders.CONTENT_TYPE);
        for (Header header : contentTypeHeaders) {
            if (expectedContentType.equals(header.getValue().replaceAll("\\s", ""))) {
                return;
            }
        }
        fail("Did not find expected content type header: " + Arrays.asList(contentTypeHeaders));
    }

    /**
     * GET app cache manifest without a supported user agent returns 404.
     */
    public void testGetManifestWithUnsupportedUserAgent() throws Exception {
        setHttpUserAgent(APPCACHE_UNSUPPORTED_USERAGENT);
        ManifestInfo manifest = getManifestInfo("/appCache/withpreload.app");

        HttpGet get = obtainGetMethod(manifest.url);
        HttpResponse httpResponse = perform(get);
        String response = getResponseBody(httpResponse);
        get.releaseConnection();

        if (!response.isEmpty()) {
            fail("manifest should be empty: *" + manifest.url);
        }
    }

    /**
     * No manifest url is given when app cache is disabled via config.
     */
    @ThreadHostileTest("disables AppCache")
    public void testGetManifestWithAppCacheDisabled() throws Exception {
        setHttpUserAgent(APPCACHE_SUPPORTED_USERAGENT);
        ServletConfigController.setAppCacheDisabled(true);
        ManifestInfo manifest = getManifestInfo("/appCache/withpreload.app");
        if (manifest.url != null) {
            fail("no manifest url should be present, but got: " + manifest.url);
        }
    }

    /**
     * Manifest url is given even when app has no explicit preloads.
     */
    public void testGetManifestForAppWithoutPreloads() throws Exception {
        setHttpUserAgent(APPCACHE_SUPPORTED_USERAGENT);
        ManifestInfo manifest = getManifestInfo("/appCache/nopreload.app");
        if (manifest.url == null) {
            fail("manifest url should be present, but got: " + manifest.url);
        }
    }

    /**
     * GET app cache manifest for app with preloads returns a full manifest containing preloading resources. * note that
     * invalid and absolute css urls are not included
     */
    public void testGetManifestForAppWithPreloads() throws Exception {
        setHttpUserAgent(APPCACHE_SUPPORTED_USERAGENT);
        ManifestInfo manifest = getManifestInfo("/appCache/withpreload.app");

        HttpGet get = obtainGetMethod(manifest.url);
        HttpResponse httpResponse = perform(get);
        String response = getResponseBody(httpResponse);
        get.releaseConnection();

        String serializedContextFragment = AuraTextUtil.urlencode(String
                .format("\"lastmod\":\"%s\"", manifest.lastmod));
        assertManifest(response, Lists.newArrayList(
                String.format(".*%s.*/app\\.css", serializedContextFragment),
                String.format(".*%s.*/app\\.js", serializedContextFragment)),
                manifest.lastmod);
    }

    /**
     * GET app cache manifest with aura.error query param returns empty response and error-valued manifest cookie.
     */
    @UnAdaptableTest
    public void testGetManifestWithAuraErrorParam() throws Exception {
        setHttpUserAgent(APPCACHE_SUPPORTED_USERAGENT);
        ManifestInfo manifest = getManifestInfo("/appCache/withpreload.app");

        HttpGet get = obtainGetMethod(getManifestErrorUrl(manifest.url));

        HttpResponse httpResponse = perform(get);
        String response = getResponseBody(httpResponse);

        assertEquals(HttpStatus.SC_NO_CONTENT, getStatusCode(httpResponse));
        assertManifestHeaders(httpResponse);

        get.releaseConnection();

        if (response != null) {
            fail("Expected empty response, but got:\n" + response);
        }
    }

    /**
     * GET app cache manifest with manifest cookie with error value, returns 404 and deletes the manifest cookie.
     */
    public void testGetManifestWithErrorManifestCookie() throws Exception {
        setHttpUserAgent(APPCACHE_SUPPORTED_USERAGENT);
        ManifestInfo manifest = getManifestInfo("/appCache/withpreload.app");

        //
        // HttpClient Cookie Example
        // 1. Add cookie
        // 2. Create HttpContext which has default CookieStore
        // 3. Perform request method with context
        //
        addCookie(getHost(), "appCache_withpreload_lm", "error", "/");
        HttpContext httpContext = getHttpCookieContext();
        HttpGet get = obtainGetMethod(manifest.url);
        HttpResponse httpResponse = perform(get, httpContext);
        int statusCode = getStatusCode(httpResponse);
        String response = getResponseBody(httpResponse);
        get.releaseConnection();

        assertEquals(HttpStatus.SC_NOT_FOUND, statusCode);
        assertManifestHeaders(httpResponse);
        assertEquals("", response);
        assertNoCookie(getHost(), "appCache_withpreload_lm", "/");

        get = obtainGetMethod(manifest.url);
        HttpResponse clean = perform(get);
        String cleanResponse = getResponseBody(clean);
        get.releaseConnection();

        // Now, after one failed call a new manifest call should go thru.(Error
        // cookie cleared);
        String serializedContextFragment = AuraTextUtil.urlencode(String
                .format("\"lastmod\":\"%s\"", manifest.lastmod));
        assertManifest(cleanResponse, Lists.newArrayList(
                String.format(".*%s.*/app\\.css", serializedContextFragment),
                String.format(".*%s.*/app\\.js", serializedContextFragment)),
                manifest.lastmod);
    }

    /**
     * GET app cache manifest with unknown format URL.
     */
    public void testGetManifestWithUnknownFormat() throws Exception {
        setHttpUserAgent(APPCACHE_SUPPORTED_USERAGENT);
        ManifestInfo manifest = getManifestInfo("/appCache/withpreload.app");

        HttpGet get = obtainGetMethod(manifest.url + "?param=unknown");
        HttpResponse httpResponse = perform(get);

        assertEquals(HttpStatus.SC_NOT_FOUND, getStatusCode(httpResponse));
        assertManifestHeaders(httpResponse);
        assertEquals("", getResponseBody(httpResponse));

        get.releaseConnection();
    }

    /**
     * GET app cache manifest for app with additional URLs specified using a controller action returns a full manifest
     * containing the additional URLs returned by controller.
     */
    public void testGetManifestForAppWithAdditionalAppCacheURLs() throws Exception {
        setHttpUserAgent(APPCACHE_SUPPORTED_USERAGENT);
        ManifestInfo manifest = getManifestInfo("/appCache/additionalUrls.app");

        HttpGet get = obtainGetMethod(manifest.url);
        HttpResponse httpResponse = perform(get);
        String response = getResponseBody(httpResponse);
        get.releaseConnection();

        String serializedContextFragment = AuraTextUtil.urlencode(String
                .format("\"lastmod\":\"%s\"", manifest.lastmod));
        assertManifest(response, Lists.newArrayList(
                String.format(".*%s.*/app\\.css", serializedContextFragment),
                String.format(".*%s.*/app\\.js", serializedContextFragment),
                "/auraFW/resources/aura/auraIdeLogo.png",
                "/auraFW/resources/aura/resetCSS.css"),
                manifest.lastmod);
    }

    /**
     * Verify behavior when action which provides additional app cache urls returns bad results or throws exception.
     * TODO: W-1590903 - What should the expected behavior be? Currently, the behavior is not consistent across these
     * scenarios, it just continues and ignores the exception thrown by the action, in other cases it returns 404.
     * Should we do something to signal that something went wrong, you might not have resources that you asked for?
     */
    public void _testGetManifestWhenAdditionalAppCacheUrlsActionBarfs() throws Exception {
        String values[] = { "{!c.throwException}", // Action throws exception
                "{!c.getString}", // Action returns literal instead of List<String>
                "{!v.attr}", // A expression that refers to attribute instead of action
                "/auraFW/resources/aura/resetCSS.css" };

        String appMarkup = String.format(baseApplicationTag,
                "useAppcache=\"true\" render=\"client\"  preload=\"appCache\" " +
                        "securityProvider=\"java://org.auraframework.java.securityProvider.LaxSecurityProvider\" " +
                        " controller=\"java://org.auraframework.impl.java.controller.TestController\" " +
                        "additionalAppCacheURLs=\"%s\"", "");

        for (String value : values) {
            DefDescriptor<ApplicationDef> desc = addSourceAutoCleanup(ApplicationDef.class,
                    String.format(appMarkup, value));
            setHttpUserAgent(APPCACHE_SUPPORTED_USERAGENT);
            ManifestInfo manifest = getManifestInfo(getUrl(desc));
            HttpGet get = obtainGetMethod(manifest.url);
            HttpResponse httpResponse = perform(get);
            String response = getResponseBody(httpResponse);
            int statusCode = getStatusCode(httpResponse);
            get.releaseConnection();

            assertEquals("Expected to fail manifest fetching. additionalAppCacheUrls:" + value,
                    HttpStatus.SC_NOT_FOUND, statusCode);

            String serializedContextFragment = AuraTextUtil.urlencode(String
                    .format("\"lastmod\":\"%s\"", manifest.lastmod));
            assertManifest(response, Lists.newArrayList(
                    String.format(".*%s.*/app\\.css", serializedContextFragment),
                    String.format(".*%s.*/app\\.js", serializedContextFragment)),
                    manifest.lastmod);
        }
    }
}
