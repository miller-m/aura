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
     * Validates the "defaultButtons" attribute.
     */
    doInit : function(cmp, evt, hlp) {

        var choice = cmp.get("v.defaultButtons");

        if (choice !== "cancel" && choice !== "confirm" && choice !== "none" && choice !== "both") {
            $A.error("The 'defaultButtons' attribute of a ui:dialogButtons " +
                     "component must be one of the following case-sensitive " +
                     "values: cancel, confirm, both, none");
        }

    },


    /*
     * Handles the click of the default cancel button of the dialog. Fires the
     * application-level event ui:closeDialog, setting the 'confirmClicked'
     * attribute to false.
     */
    cancel : function(cmp, evt, hlp) {

        hlp.confirmOrCancel(cmp.get("v._parentDialog"), false);

    },


    /*
     * Handles the click of default confirm button of the dialog. Fires the
     * application-level event ui:closeDialog, setting the 'confirmClicked'
     * attribute to true.
     */
    confirm : function(cmp, evt, hlp) {

        hlp.confirmOrCancel(cmp.get("v._parentDialog"), true);

    }


})
