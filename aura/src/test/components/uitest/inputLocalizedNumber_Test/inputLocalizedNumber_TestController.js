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
    submit : function(component, event) {
        var cmpType = component.get("v.cmpType");
        var inputCmpValue = component.get("inputValue").get("v.value");
        var a = component.get("c.echo" + cmpType);
        a.setParams({
            inVar : inputCmpValue
        });

        a.setCallback(component, function(action){
            var retValue;
            if (action.getState() === "SUCCESS") {
                retValue = action.getReturnValue();
            } else {
                retValue = action.getError().message;
            }
            component.find("outputValue").getAttributes().setValue("value", retValue);
        });

        $A.enqueueAction(a);
    }
}
