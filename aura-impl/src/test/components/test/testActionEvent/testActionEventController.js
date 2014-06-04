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
    fireEvent: function(cmp, event) {
        var a = cmp.get("c.throwsCSE");
        a.setParams({
            event: cmp.get("v.eventName"),
            paramName: cmp.get("v.eventParamName"),
            paramValue: cmp.get("v.eventParamValue")
        });
        a.setCallback(cmp, function(action){
            cmp.getAttributes().setValue("response", action);
        });
        $A.enqueueAction(a);
    },

    showSystemErrorEvent: function(cmp, event) {
        $A.log(cmp);
        $A.log(event);
        cmp.getValue("v.event").setValue(event.getDef().getDescriptor().getQualifiedName());
        cmp.getValue("v.data").setValue(event.getParam("message"));
    },

    showLocalEvent: function(cmp, event) {
        $A.log(cmp);
        $A.log(event);
        cmp.getValue("v.event").setValue(event.getDef().getDescriptor().getQualifiedName());
        cmp.getValue("v.data").setValue(event.getParam("msg"));
    }
})
