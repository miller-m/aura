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

import java.util.Map;

import org.auraframework.Aura;
import org.auraframework.builder.StyleDefBuilder;
import org.auraframework.def.ComponentDef;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.StyleDef;
import org.auraframework.service.BuilderService;
import org.auraframework.service.DefinitionService;
import org.auraframework.system.Annotations.Key;
import org.auraframework.util.AuraTextUtil;

import com.google.common.collect.Maps;

/**
 */
public class CreateStyleDefQuickFix extends AuraQuickFix {

    public CreateStyleDefQuickFix(Map<String, Object> attributes) {
        super("Create Style Definition", attributes, Aura.getDefinitionService().getDefDescriptor(
                "auradev:createStyleDefQuickFix", ComponentDef.class));
    }

    public CreateStyleDefQuickFix(DefDescriptor<?> descriptor) {
        this(createMap(descriptor));
    }

    private static Map<String, Object> createMap(DefDescriptor<?> descriptor) {
        Map<String, Object> ret = Maps.newHashMap();
        ret.put("descriptor", descriptor);
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void fix() throws QuickFixException {
        BuilderService builderService = Aura.getBuilderService();
        DefinitionService definitionService = Aura.getDefinitionService();

        DefDescriptor<StyleDef> styleDescriptor = (DefDescriptor<StyleDef>) getAttributes().get("descriptor");
        StyleDefBuilder builder = builderService.getStyleDefBuilder();

        builder.setDescriptor(styleDescriptor);
        builder.setClassName(styleDescriptor.getNamespace() + AuraTextUtil.initCap(styleDescriptor.getName()));
        StyleDef styleDef = builder.build();
        definitionService.save(styleDef);
    }

    public static final void doFix(@Key("attributes") Map<String, Object> attributes) throws QuickFixException {
        new CreateComponentDefQuickFix(attributes).fix();
    }
}
