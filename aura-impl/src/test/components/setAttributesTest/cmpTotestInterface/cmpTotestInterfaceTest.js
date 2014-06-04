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
({
    /**
     * Verify that attributes of parent interface can be set by implementing component.
     */
    // TODO W-1468465 - cannot set attribute values on interface
    _testSettingAttributeValueInheritedFromInterface:{
        test:function(cmp){
            //Access the interface included as facet in the current component.
            var testCmp = cmp.find('id');
            aura.test.assertNotNull(testCmp);
            //It should actually be the implementing component because the provider would have injected the implementation.
            aura.test.assertEquals('markup://setAttributesTest:implementation',testCmp.getDef().getDescriptor().getQualifiedName());
            aura.test.assertEquals('implementationX',testCmp.getValue('v.SimpleAttribute').getValue());
            //Verify the content of the HTML element
            aura.test.assertEquals('The value of SimpleAttribute = implementationX',$A.test.getText(testCmp.getElement()));
        }
    }
})