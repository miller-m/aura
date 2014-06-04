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
package org.auraframework.integration;

import java.io.IOException;
import java.util.Map;

import org.auraframework.throwable.AuraRuntimeException;
import org.auraframework.throwable.quickfix.QuickFixException;

/**
 * An Integration defines the scope of creating a set of component injection
 * scripts
 *
 * @author dchasman
 * @since 184
 */
public interface Integration {
    /**
     * Generates a &lt;script&gt; tag that will correctly embed an instance of an
     * Aura
     * component bound to the DOM element identified by localDomId.
     *
     * @param tag
     *            Fully qualified (namespace:name) name of the Aura component.
     * @param attributes
     *            A map of the component's attributes as key/value pairs.
     * @param localId
     *            The user-provided locally unique ID of this component
     *            that can be used with $A.getRoot().find(localId).
     * @param locatorDomId
     *            The DOM identifier for the element that will be used as the
     *            parent of the component's elements.
     * @param out
     *            Destination for injection script content.
     * @throws AuraRuntimeException
     * @throws IOException
     * @throws QuickFixException
     */
    void injectComponent(String tag, Map<String, Object> attributes, String localId, String locatorDomId, Appendable out)
            throws UnsupportedUserAgentException, AuraRuntimeException,
            IOException, QuickFixException;

    @Deprecated
    /**
     * @deprecated This method will soon be replaced by a new dependencies framework.
     * 
     * Preloads the metadata definitions of all dependent components that should 
     * be sent to the client for the specified namespace.    
     * 
     * @param namespace The namespace for which to preload metadata definitions on the client. 
     */
    void addPreload(String namespace);
}
