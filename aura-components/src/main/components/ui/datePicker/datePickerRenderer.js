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
    afterRender: function(component, helper) {
        var visible = component.get("v.visible");
        if (visible === true) {
            helper.setGridInitialValue(component);
            helper.updateMonthYear(component, component.get("v.value"));
            helper.updateGlobalEventListeners(component);
        }
        var ret = this.superAfterRender();
        if (visible === true) {
            helper.localizeToday(component);
        }
        return ret;
    },

    rerender: function(component, helper) {
        var visible = component.get("v.visible");
        if (visible === true) {
            helper.setGridInitialValue(component);
            helper.updateMonthYear(component, component.get("v.value"));
            helper.updateGlobalEventListeners(component);
        }
        this.superRerender();
        if (visible === true) {
            helper.localizeToday(component);
        }
    },
    
    unrender: function(component, helper) {
        if (helper.getOnClickEventProp.cache && 
            helper.getOnClickEventProp.cache.onClickStartEvent && 
            component._onClickStartFunc) {
            if (document.body.removeEventListener) {
                document.body.removeEventListener(helper.getOnClickEventProp.cache.onClickStartEvent, component._onClickStartFunc, false);
            } else {
                if (document.body.detachEvent) {
                    document.body.detachEvent('on' + helper.getOnClickEventProp.cache.onClickStartEvent, component._onClickStartFunc);
                }
            }
        }
        if (helper.getOnClickEventProp.cache &&
            helper.getOnClickEventProp.cache.onClickEndEvent && 
            component._onClickEndFunc) {
            if (document.body.removeEventListener) {
                document.body.removeEventListener(helper.getOnClickEventProp.cache.onClickEndEvent, component._onClickEndFunc, false);
            } else {
                if (document.body.detachEvent) {
                    document.body.detachEvent('on' + helper.getOnClickEventProp.cache.onClickEndEvent, component._onClickEndFunc);
                }
            }
        }
        this.superUnrender();
    }
})