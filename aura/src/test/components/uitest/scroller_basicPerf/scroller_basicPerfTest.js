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
	testScrollerPerf: {
		attributes: {},
        test: function(component){
        	var destroyed,
        		totalScrollerComponentsInDOM,
        		totalOutputURLComponentsInDOM,
        		queryString = $A.getQueryStatement().from("component")
							.field("descriptor", "getDef().getDescriptor().toString()")
							.groupBy("descriptor");
        	
        	totalScrollerComponentsInDOM  = queryString.query().groups['markup://ui:scroller'];
        	totalOutputURLComponentsInDOM  = queryString.query().groups['markup://ui:outputURL'];
        	
        	if(totalScrollerComponentsInDOM && totalOutputURLComponentsInDOM){
        		aura.test.assertEquals(2, totalScrollerComponentsInDOM.length);
        		aura.test.assertEquals(500, totalOutputURLComponentsInDOM.length);
        	}
        	else{
        		aura.test.assert(false, "DOM query returned empty.");
        	}
        	destroyed = $A.getRoot().find("scrollContainer").getValue("v.body").remove(0).destroy();
        	
        	aura.test.addWaitFor(destroyed, function(){return "7:2.a";}, function(){
        		totalScrollerComponentsInDOM  = queryString.query().groups['markup://ui:scroller'];
            	totalOutputURLComponentsInDOM  = queryString.query().groups['markup://ui:outputURL'];
            	
            	aura.test.assertUndefined(totalScrollerComponentsInDOM);
            	aura.test.assertUndefined(totalOutputURLComponentsInDOM);
        	});
        }
	}
})