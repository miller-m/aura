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
{
    runTests : function(cmp, event, helper) {
        var testsProps = cmp.get("m.testsWithProps");
        var tt = [];
        cmp.getValue("m.testsWithProps").each(function(map){
            if(map.getValue("selected").getBooleanValue() && map.get("isHidden") === ''){
                tt.push(map.get("name"));
                map.getValue("status").setValue("ENQUEUEING");
                map.getValue("exception").setValue("");
            }});

        var a = cmp.get("c.runTestSet");

        a.setParams({ testSet: tt });
        a.setCallback(cmp, function(action){
            helper.raisePollEvent(cmp, 5000);
        });
        $A.enqueueAction(a);
    },

     poll : function (cmp, evt, helper){
        var a = cmp.get("c.pollForTestRunStatus");
        a.setAbortable(true);
        var pollFreq = 60000;
        a.setCallback(cmp, function(action){

            if (action.getState() === "SUCCESS") {
                var rValue = action.getReturnValue();
                cmp.getValue("m.testsWithProps").each(function(map){
                    map.getValue("status").setValue(rValue['testsWithPropsMap'][map.get("name")].status);
                    map.getValue("exception").setValue(rValue['testsWithPropsMap'][map.get("name")].exception);
                });
                if(rValue['testsRunning']){
                    pollFreq =  5000;
                }
            }

            helper.raisePollEvent(cmp, pollFreq);

        });
        $A.enqueueAction(a);
     },

     toggleRunAllTests : function (cmp){
         var runAllTestsCmp = cmp.find("runAllTests");
         var shouldRunAllTests = runAllTestsCmp.get("v.value");
         cmp.getValue("m.testsWithProps").each(function(map){
             map.getValue("selected").setValue(shouldRunAllTests);
         });
     },

     filterOnSearchText : function (cmp, event, helper){
     	helper.updateDisplay(cmp, helper);
     },

    toggleShowFailedTest : function(cmp, event){
        //Short circuiting re-render to avoid lag
        event.getSource().getValue("v.value").commit();
    },

     toggleShowFailedTests : function(cmp, event, helper){
     	helper.updateDisplay(cmp, helper);
     },
     
     toggleShowOnlyIntegrationTests: function(cmp, event, helper){
     	helper.updateDisplay(cmp, helper);
     }
}
