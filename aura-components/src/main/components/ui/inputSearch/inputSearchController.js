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
    clearSearchTerm: function(component, event, helper) {
        var searchCmp = component.find("search");
        var el = searchCmp.getElement();
        if (el) {
            el.value = "";
            component.getValue("v.value").setValue("");
            helper.toggleClearButton(component);
        }
        var e = component.getEvent("search");
        e.setParams({
            searchTerm: ""
        });
        e.fire();
    },
    
    input: function(component, event, helper) {
        helper.toggleClearButton(component);
    }
})