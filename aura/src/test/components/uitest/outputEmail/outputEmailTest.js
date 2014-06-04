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
     * Verify undefined value renders nothing.
     */
    testValueUndefined: {
        test: function(component){
            var cmp = component.find("undefined");
            $A.test.assertEquals(null, cmp.getElement(), "unexpected elements");
        }
    },

    /**
     * Verify null value renders nothing.
     */
    testValueNull: {
        test: function(component){
            var cmp = component.find("null");
            $A.test.assertEquals(null, cmp.getElement(), "unexpected elements");
        }
    }
})
