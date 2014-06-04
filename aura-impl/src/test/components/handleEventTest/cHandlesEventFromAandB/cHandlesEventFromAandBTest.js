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
    testIsBCorrectCmp:{
        test:function(cmp){
            aura.test.assertEquals("true", cmp.getSuper().find("isBCmpCorrect").getElement().innerHTML);
        }
    },

    testAIsCorrectCmp:{
        test:function(cmp){
            aura.test.assertEquals("true", cmp.getSuper().getSuper().find("isCmpCorrect").getElement().innerHTML);
        }
    },

    testIsBCorrectCmpEvtB:{
        test:function(cmp){
            aura.test.assertEquals("true", cmp.getSuper().find("isBCmpCorrectEvtB").getElement().innerHTML);
        }
    },

    testIsCCorrectCmpEvtB:{
        test:function(cmp){
            aura.test.assertEquals("true", cmp.find("isCCmpCorrect").getElement().innerHTML);
        }
    }
})
