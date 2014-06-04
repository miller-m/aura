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
/*jslint sub: true */
/**
 * @namespace Creates a HelperDef instance.
 * @constructor
 * @protected
 */
function HelperDef(config, zuper){
    var functions = config["functions"] || {};
    for(var k in functions){
        functions[k] = aura.util.json.decodeString(functions[k]);
    }
    this.functions = functions;

    if(zuper){
        for(var key in zuper){
            if(!functions[key]){
                functions[key] = zuper[key];
            }
        }
    }

}

HelperDef.prototype.auraType = "HelperDef";

/**
 * Returns the functions for HelperDef.
 */
HelperDef.prototype.getFunctions = function HelperDef$getFunctions(){
    return this.functions;
};
//#include aura.helper.HelperDef_export
