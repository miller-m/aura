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
package org.auraframework.impl.java.provider;

import org.auraframework.Aura;
import org.auraframework.def.ComponentDef;
import org.auraframework.def.ComponentDescriptorProvider;
import org.auraframework.def.DefDescriptor;
import org.auraframework.impl.system.DefDescriptorImpl;
import org.auraframework.instance.BaseComponent;
import org.auraframework.system.Annotations.Provider;

/**
 * An interface provider used for testing.
 * 
 * 
 * @since 0.0.196
 */
@Provider
public class TestComponentDescriptorProvider implements ComponentDescriptorProvider {
    @Override
    public DefDescriptor<ComponentDef> provide() {
        BaseComponent<?, ?> component = Aura.getContextService().getCurrentContext().getCurrentComponent();
        String num = (String) component.getAttributes().getExpression("implNumber");
        return DefDescriptorImpl.getInstance("test:test_Provider_providerImpl" + num, ComponentDef.class);
    }
}
