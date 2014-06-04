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
package org.auraframework.impl.model;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.auraframework.Aura;
import org.auraframework.def.ComponentDef;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.ModelDef;
import org.auraframework.impl.AuraImplTestCase;
import org.auraframework.impl.expression.PropertyReferenceImpl;
import org.auraframework.impl.javascript.model.JavascriptModelDef;
import org.auraframework.impl.javascript.model.JavascriptValueDef;
import org.auraframework.impl.system.DefDescriptorImpl;
import org.auraframework.instance.Model;
import org.auraframework.throwable.quickfix.InvalidDefinitionException;

import com.google.common.collect.Maps;

public class JavascriptModelDefTest extends AuraImplTestCase {

    public JavascriptModelDefTest(String name) {
        super(name);
    }

    /**
     * Verify that javascript model defs are serializable.
     */
    public void testDefaults() throws Exception {
        // Find the model by autowiring
        Aura.getDefinitionService().getDefinition("test:jsModel", ComponentDef.class);
        JavascriptModelDef modelDef = (JavascriptModelDef) Aura.getDefinitionService().getDefinition(
                "js://test.jsModel", ModelDef.class);

        // Make sure the properties expected are found on the def
        validateProperty(modelDef, "obj", Maps.newHashMap());
        validateProperty(modelDef, "bool", true);
        validateProperty(modelDef, "num", BigDecimal.valueOf(5));
        validateProperty(modelDef, "str", "yes");
        validateProperty(modelDef, "list", new ArrayList<Object>());
    }

    /**
     * Verify bad value in javascript model throws correct exception.
     */
    public void testBadModelValue() throws Exception {
        DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(ComponentDef.class, "<aura:component/>");
        DefDescriptor<ModelDef> modelDesc = DefDescriptorImpl.getAssociateDescriptor(cmpDesc, ModelDef.class,
                DefDescriptor.JAVASCRIPT_PREFIX);
        addSourceAutoCleanup(modelDesc, "{\"badType\":function(){}}");
        try {
            cmpDesc.getDef();
            fail("Bad value in javascript model should throw Exception");
        } catch (Exception e) {
            checkExceptionFull(e, InvalidDefinitionException.class, "Invalid value type in model definition.");
        }
    }

    /**
     * Verify null value in javascript model throws correct exception. We don't allow nulls in js models because we
     * derive the types from the value since Json gives no other type data. See W-1594517.
     */
    // TODO(W-1594517): Add validation to block null values
    public void _testNullValue() throws Exception {
        DefDescriptor<ComponentDef> cmpDesc = addSourceAutoCleanup(ComponentDef.class, "<aura:component/>");
        DefDescriptor<ModelDef> modelDesc = DefDescriptorImpl.getAssociateDescriptor(cmpDesc, ModelDef.class,
                DefDescriptor.JAVASCRIPT_PREFIX);
        addSourceAutoCleanup(modelDesc, "{\"nullType\":null}");
        try {
            cmpDesc.getDef();
            fail("Null value in javascript model should throw Exception");
        } catch (Exception e) {
            checkExceptionFull(e, InvalidDefinitionException.class, "Null value type in model definition.");
        }
    }

    private void validateProperty(JavascriptModelDef def, String name, Object expectedValue) throws Exception {
        // Validate the default from the def
        JavascriptValueDef valueDef = (JavascriptValueDef) def.getMemberByName(name);
        Object defaultValue = valueDef.getDefaultValue();
        assertEquals(expectedValue, defaultValue);

        // Also validate that the default appropriately shows up on an instance
        // of this model.
        Model model = def.newInstance();
        Object instanceValue = model.getValue(new PropertyReferenceImpl(name, null));
        assertEquals(expectedValue, instanceValue);

        // Make sure that the instance value was cloned so that changes to the
        // instance value don't affect other instances or the default.
        // This check doesn't work well for booleans or Strings.
        if (!(expectedValue instanceof Boolean) && !(expectedValue instanceof String)) {
            assertNotSame(String.format(
                    "The default value %s and instance value %s should not have the same identity.", defaultValue,
                    instanceValue), defaultValue, instanceValue);
        }
    }

}
