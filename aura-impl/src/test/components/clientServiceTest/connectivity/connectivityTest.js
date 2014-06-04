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
	/** IE & FIREFOX are excluded:The tests try to send out a request to other domains http://invalid.salesforce.com, 
	 * IE and Firefox block it by default
	 */
    browsers:["GOOGLECHROME","SAFARI"],
    /**
     * Calling server action on default host succeeds.
     */
    testConnection : {
        attributes : { __layout : "#" },
        test : [function(component) {
                // we assume we start connected
                $A.test.assertTrue($A.clientService.isConnected());
                $A.test.addWaitFor(true, function() { return aura.util.trim(component.get("v.eventsFired")) == "layoutChange"; });
            }, function(component) {
                component.find("button").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return component.get("v.actionStatus") != ""; });
            }, function(component) {
                $A.test.assertEquals("SUCCESS", component.get("v.actionStatus"));
                $A.test.assertEquals("layoutChange", component.get("v.eventsFired"));
                // ensure we still think we're connected
                $A.test.assertTrue($A.clientService.isConnected());
            }]
    },

    /**
     * Calling server action on unknown host throws connectionLost event.
     */
    testConnectionLost : {
        testLabels : ["UnAdaptableTest"],
        attributes : { __layout : "#" },
        test : [function(component) {
                $A.test.addWaitFor(true, function() { return aura.util.trim(component.get("v.eventsFired")) == "layoutChange"; });
            }, function(component) {
                $A.test.setTestTimeout(30000);
                component.getValue("v.host").setValue("http://invalid.salesforce.com");
                component.find("button").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return component.get("v.actionStatus") != ""; });
            }, function(component) {
                $A.test.assertEquals("INCOMPLETE", component.get("v.actionStatus"));
                $A.test.assertEquals("layoutChange connectionLost", aura.util.trim(component.get("v.eventsFired")));
                $A.test.assertFalse($A.clientService.isConnected());
                component.find("button").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return component.get("v.actionStatus") != ""; });
            }, function(component) {
                // connectionLost event is not repeated
                $A.test.assertEquals("layoutChange connectionLost", aura.util.trim(component.get("v.eventsFired")));
                // still offline
                $A.test.assertFalse($A.clientService.isConnected());
            }]
    },

    /**
     * Test setting connected false generates connectionLost event.  Subsequent actions will succeed and generate
     * connectionResumed event
     */
    testSetConnectedFalse : {
        testLabels : ["UnAdaptableTest"],
        attributes : { __layout : "#" },
        test : [function(component) {
                // we assume we start connected
                $A.test.assertTrue($A.clientService.isConnected());            
                $A.test.addWaitFor(true, function() { return aura.util.trim(component.get("v.eventsFired")) == "layoutChange"; });
             }, function(component) {
                component.find("setConnectedFalseButton").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return aura.util.trim(component.get("v.eventsFired")) == "layoutChange connectionLost"; });
             }, function(component) {
                $A.test.assertFalse($A.clientService.isConnected());
                component.find("button").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return component.get("v.actionStatus") == "SUCCESS"; });
            }, function(component) {
                $A.test.assertEquals("layoutChange connectionLost connectionResumed", aura.util.trim(component.get("v.eventsFired")));
                $A.test.assertTrue($A.clientService.isConnected());
            }]
    },

    /**
     * Test setting connected false after disconnect does not generate connectionLost event.
     */
    testSetConnectedFalseAfterDisconnect : {
        testLabels : ["UnAdaptableTest"],
        attributes : { __layout : "#" },
        test : [function(component) {
                // we assume we start connected
                $A.test.assertTrue($A.clientService.isConnected());            
                $A.test.addWaitFor(true, function() { return aura.util.trim(component.get("v.eventsFired")) == "layoutChange"; });
            }, function(component) {
                $A.test.setTestTimeout(30000);
                component.getValue("v.host").setValue("http://invalid.salesforce.com");
                component.find("button").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return component.get("v.actionStatus") != ""; });
            }, function(component) {
                $A.test.assertEquals("INCOMPLETE", component.get("v.actionStatus"));
                $A.test.assertEquals("layoutChange connectionLost", aura.util.trim(component.get("v.eventsFired")));
                $A.test.assertFalse($A.clientService.isConnected());
            }, function(component) {
                component.find("setConnectedFalseButton").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return aura.util.trim(component.get("v.eventsFired")) == "layoutChange connectionLost"; });
            }, function(component) {
                $A.test.assertFalse($A.clientService.isConnected());
                component.getValue("v.host").setValue(undefined); // restore to default
                component.find("button").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return component.get("v.actionStatus") == "SUCCESS"; });
            }, function(component) {
                // ensure there are not 2 connectionLost events generated before a resume
                $A.test.assertEquals("layoutChange connectionLost connectionResumed", aura.util.trim(component.get("v.eventsFired")));
                $A.test.assertTrue($A.clientService.isConnected());
            }]
    },
    /**
     * Test setting connected false then disconnect does not generate connectionLost event.
     */
    testSetConnectedFalseThenDisconnect : {
        testLabels : ["UnAdaptableTest"],
        attributes : { __layout : "#" },
        test : [function(component) {
                // we assume we start connected
                $A.test.assertTrue($A.clientService.isConnected());            
                $A.test.addWaitFor(true, function() { return aura.util.trim(component.get("v.eventsFired")) == "layoutChange"; });
            }, function(component) {
                component.find("setConnectedFalseButton").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return aura.util.trim(component.get("v.eventsFired")) == "layoutChange connectionLost"; });
            }, function(component) {
                $A.test.assertFalse($A.clientService.isConnected());
                $A.test.setTestTimeout(30000);
                component.getValue("v.host").setValue("http://invalid.salesforce.com");
                component.find("button").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return component.get("v.actionStatus") != ""; });
            }, function(component) {
                $A.test.assertEquals("INCOMPLETE", component.get("v.actionStatus"));
                $A.test.assertEquals("layoutChange connectionLost", aura.util.trim(component.get("v.eventsFired")));
                $A.test.assertFalse($A.clientService.isConnected());
            }, function(component) {
                component.getValue("v.host").setValue(undefined); // restore to default
                component.find("button").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return component.get("v.actionStatus") == "SUCCESS"; });
            }, function(component) {
                // ensure there are not 2 connectionLost events generated before a resume
                $A.test.assertEquals("layoutChange connectionLost connectionResumed", aura.util.trim(component.get("v.eventsFired")));
                $A.test.assertTrue($A.clientService.isConnected());
            }]
    },
 
    /**
     * Calling server action succeeds after a prior connection failure.
     */
    testConnectionResumed : {
        testLabels : ["UnAdaptableTest"],
        attributes : { __layout : "#" },
        test : [function(component) {
                $A.test.addWaitFor(true, function() { return aura.util.trim(component.get("v.eventsFired")) == "layoutChange"; });
            }, function(component) {
                $A.test.setTestTimeout(30000);
                component.getValue("v.host").setValue("http://invalid.salesforce.com");
                component.find("button").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return component.get("v.actionStatus") != ""; });
            }, function(component) {
                $A.test.assertEquals("INCOMPLETE", component.get("v.actionStatus"));
                $A.test.assertEquals("layoutChange connectionLost", aura.util.trim(component.get("v.eventsFired")));
                $A.test.assertFalse($A.clientService.isConnected());
                component.getValue("v.host").setValue(undefined); // restore to default
                component.find("button").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return component.get("v.actionStatus") == "SUCCESS"; });
            }, function(component) {
                $A.test.assertEquals("layoutChange connectionLost connectionResumed", aura.util.trim(component.get("v.eventsFired")));
                $A.test.assertTrue($A.clientService.isConnected());
                component.find("button").get("e.press").fire();
                $A.test.addWaitFor(true, function() { return component.get("v.actionStatus") == "SUCCESS"; });
            }, function(component) {
                // connectionResumed event is not repeated
                $A.test.assertEquals("layoutChange connectionLost connectionResumed", aura.util.trim(component.get("v.eventsFired")));
                // still online
                $A.test.assertTrue($A.clientService.isConnected());
            }]
    },

    /**
     * Changing layout with no connection throws connectionLost and layoutFailed events.
     */
    testConnectionLostForLayout : {
        testLabels : ["UnAdaptableTest"],
        attributes : { host : "http://invalid.salesforce.com", __layout : "#" },
        test : [function(component) {
                $A.test.addWaitFor(true, function() { return aura.util.trim(component.get("v.eventsFired")) == "connectionLost layoutFailed layoutChange"; });
            }, function(component) {
                $A.test.assertFalse($A.clientService.isConnected());
            }]
    },

    /**
     * Changing layout after a prior connection failure succeeds.
     * Excluding IE: history service was not previously supported in IE
     */
    testConnectionResumedForLayout : {
        testLabels : ["UnAdaptableTest"],
        attributes : { host : "http://invalid.salesforce.com", __layout : "#" },
        test : [function(component) {
                $A.test.addWaitFor(true, function() { return aura.util.trim(component.get("v.eventsFired")) == "connectionLost layoutFailed layoutChange"; });
            }, function(component) {
                $A.test.assertFalse($A.clientService.isConnected());
                component.getValue("v.host").setValue(undefined); // restore to default
                $A.historyService.set("action");
                $A.test.addWaitFor(true, function() { return aura.util.trim(component.get("v.eventsFired")) == "connectionLost layoutFailed layoutChange connectionResumed layoutChange"; });
            }, function(component) {
                $A.test.assertTrue($A.clientService.isConnected());
            }]
    }
})
