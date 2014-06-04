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
    render : function(cmp, helper){
        cmp.renderHelper = helper;
        return this.superRender();
    },

    afterRender : function(cmp, helper){
        cmp.afterRenderHelper = helper;
        this.superAfterRender();
    },

    rerender : function(cmp, helper){
        cmp.rerenderHelper = helper;
        this.superRerender();
    },

    unrender : function(cmp, helper){
        cmp.unrenderHelper = helper;
        this.superUnrender();
    }
})
