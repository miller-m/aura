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
    testOptimizeWhitespace : {
        test : function(cmp) {
            var cmpElems = cmp.getElements();
            var divText = cmp.find("divText").getElement().innerHTML;

            // check no whitespace elements present, only 'true', 'false', and the div elements
            $A.test.assertEquals("true", $A.test.getText(cmpElems[0]), "First element in body of component should be 'true'.");
            $A.test.assertEquals("false", $A.test.getText(cmpElems[1]), "Second element in body of component should be 'false'.");
            $A.test.assertTrue($A.test.isInstanceOfDivElement(cmpElems[2]), "Third element in body of component should be div");

            $A.test.assertEquals("truefalse", divText, "Whitespace for text within div should not be present.");
        }
    }
})