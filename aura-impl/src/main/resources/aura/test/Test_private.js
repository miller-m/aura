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
/*jslint sub:true */

var priv = {
        /* Complete and errors are used in case Tests invoke actions on the server. Such actions have callback functions. These
         * two variables help in accounting for assertions in the call back functions.
         */
        waits : [],
        cleanups : [],
        completed : {}, // A map of action name to boolean for 'named' actions that have been queued
        inProgress : -1, // -1:uninitialized, 0:complete, 1:tearing down, 2:running, 3+:waiting
        errors : [],
        preErrors : [],
        preWarnings : [],
        expectedErrors : [],
        expectedWarnings : [],
        timeoutTime : 0,
        appCacheEvents : [], // AppCache events in order, as they are picked up

        handleAppcacheChecking : function() {
            priv.appCacheEvents.push("checking");
        },

        handleAppcacheProgress : function() {
            priv.appCacheEvents.push("progress");
        },

        handleAppcacheDownloading: function() {
            priv.appCacheEvents.push("downloading");
        },

        handleAppcacheCached: function() {
            priv.appCacheEvents.push("cached");
        },

        handleAppcacheError: function() {
            priv.appCacheEvents.push("error");
        },

        putMessage: function(pre, expected, msg) {
            for (var i = 0; i < expected.length; i++) {
                if (expected[i] === undefined) {
                    continue;
                }
                if (msg.indexOf(expected[i]) === 0) {
                    expected[i] = undefined;
                    return true;
                }
            }
            if (pre !== null) {
                pre.push(msg);
                return true;
            }
            return false;
        },

        expectMessage: function(pre, expected, msg) {
            if (pre !== null) {
                for (var i = 0; i < pre.length; i++) {
                    if (pre[i] === undefined) {
                        continue;
                    }
                    if (pre[i].indexOf(msg) === 0) {
                        pre[i] = undefined;
                        return;
                    }
                }
            }
            expected.push(msg);
        }
};

/**
 * Register a global error handler to catch uncaught javascript errors.
 * @ignore
 */
window.onerror = (function(){
    var origHandler = window.onerror;
    /** @inner */
    var newHandler = function(msg, url, line){
        var error = { message: "Uncaught js error: " + msg };
        if(url){
            error["url"] = url;
        }
        if(line){
            error["line"] = line;
        }
        priv.errors.push(error);
    };

    if(origHandler) {
        return function(){ return origHandler.apply(this, arguments) || newHandler.apply(this, arguments); };
    } else {
        return newHandler;
    }
})();

/**
 * Used to keep track of errors happening in test modes.
 * @private
 */
function logError(msg, e){
    var err;
    var p;

    if (e) {
        err = { "message": msg + ": " + (e.message || e.toString()) };
        for (p in e){
            if(p=="message"){
                continue;
            }
            err[p] = "" + e[p];
        }
    } else {
        err = { "message": msg };
    }
    priv.errors.push(err);
}

/**
 * Run the test
 * @private
 */
function run(name, code, count){
    // check if test has already started running, since frame loads from layouts may trigger multiple runs
    if(priv.inProgress >= 0){
        return;
    }
    priv.inProgress = 2;
    priv.timeoutTime = new Date().getTime() + 5000 * count;
    if(!count){
        count = 1;
    }
    var cmp = $A.getRoot();
    var suite = aura.util.json.decode(code);
    var stages = suite[name]["test"];
    stages = $A.util.isArray(stages) ? stages : [stages];

    /** @inner */
    var doTearDown = function() {
        var i;

        // check if already tearing down
        if(priv.inProgress > 1){
            priv.inProgress = 1;
        }else {
            return;
        }
        try {
            for (i = 0; i < priv.cleanups.length; i++) {
                priv.cleanups[i]();
            }
        } catch(ce){
            logError("Error during cleanup", ce);
        }
        try{
            if(suite["tearDown"]){
                suite["tearDown"].call(suite, cmp);
            }
            setTimeout(function(){priv.inProgress--;}, 100);
        }catch(e){
            logError("Error during tearDown", e);
            priv.inProgress = 0;
        }
    };

    var logErrors = function(fn, label, errorArray) {
        var i;

        if (errorArray !== null && errorArray.length > 0) {
            for (i = 0; i < errorArray.length; i++) {
                if (errorArray[i] !== undefined) {
                    fn(label+errorArray[i]);
                }
            }
        }
    };
    
    /** @inner */
    var continueWhenReady = function() {
        var i;

        if(priv.inProgress < 2){
            return;
        }
        if(priv.errors.length > 0){
            doTearDown();
            return;
        }
        try{
            if((priv.inProgress > 1) && (new Date().getTime() > priv.timeoutTime)){
                if(priv.waits.length > 0){
                    var texp = priv.waits[0].expected;
                    if($A.util.isFunction(texp)){
                        texp = texp().toString();
                    }
                    var tact = priv.waits[0].actual;
                    var val = tact;
                    if($A.util.isFunction(tact)){
                        val = tact().toString();
                        tact = tact.toString();
                    }
                    var failureMessage = "";
                    if(!$A.util.isUndefinedOrNull(priv.waits[0].failureMessage)){
                    	failureMessage = "; Failure Message: " + priv.waits[0].failureMessage;
                    }
                    throw new Error("Test timed out waiting for: " + tact + "; Expected: " + texp + "; Actual: " + val + failureMessage);
                }else{
                    throw new Error("Test timed out");
                }
            }
            if(priv.inProgress > 2){
                setTimeout(continueWhenReady, 200);
            }else{
                if(priv.waits.length > 0){
                    var exp = priv.waits[0].expected;
                    if($A.util.isFunction(exp)){
                        exp = exp();
                    }
                    var act = priv.waits[0].actual;
                    if($A.util.isFunction(act)){
                        act = act();
                    }
                    if(exp === act){
                        var callback = priv.waits[0].callback;
                        if(callback){
                            //Set the suite as scope for callback function.
                            //Helpful to expose test suite as 'this' in callbacks for addWaitFor
                            callback.call(suite);
                        }
                        priv.waits.shift();
                        setTimeout(continueWhenReady, 1);
                    }else{
                        setTimeout(continueWhenReady, 200);
                    }
                } else {
                    logErrors(logError, "Did not receive expected error:",priv.expectedErrors);
                    priv.expectedErrors = [];

                    logErrors(logError, "Did not receive expected warning:",priv.expectedWarnings);
                    priv.expectedWarnings = [];

                    if (stages.length === 0){
                        doTearDown();
                    } else {
                        priv.lastStage = stages.shift();
                        priv.lastStage.call(suite, cmp);
                        setTimeout(continueWhenReady, 1);
                    }

                    logErrors(logError, "Received unexpected error:",priv.preErrors);
                    priv.preErrors = null;

                    logErrors(function(str) { $A.log(str); }, "Received unexpected warning:",priv.preWarnings);
                    priv.preWarnings = null;
                }
            }
        }catch(e){
            if(priv.lastStage) {
                e["lastStage"] = priv.lastStage;
            }
            logError("Test error", e);
            doTearDown();
        }
    };
    try {
        if(suite["setUp"]){
            suite["setUp"].call(suite, cmp);
        }
    }catch(e){
        logError("Error during setUp", e);
        doTearDown();
    }
    setTimeout(continueWhenReady, 1);
}

/**
 * Provide some information about the current state of the test.
 * @private
 */
function getDump() {
    var status = "";
    if (priv.errors.length > 0) {
        status += "errors {" + $A.test.print($A.test.getErrors()) + "} ";
    }
    if (priv.waits.length > 0 ) {
        var actual;
        try {
            actual = priv.waits[0].actual();
        } catch (e) {}
        var failureMessage = "";
        if(!$A.util.isUndefinedOrNull(priv.waits[0].failureMessage)){
        	failureMessage = " Failure Message: {" + priv.waits[0].failureMessage + "}";
        }
        status += "waiting for {" + $A.test.print(priv.waits[0].expected) + "} currently {" + $A.test.print(actual) + "}" + failureMessage + " from {" + $A.test.print(priv.waits[0].actual) + "} after {" + $A.test.print(priv.lastStage) + "} ";
    } else if (!$A.util.isUndefinedOrNull(priv.lastStage)) {
        status += "executing {" + $A.test.print(priv.lastStage) + "} ";
    }
    return status;
}

/**
 * Set up AppCache event listeners. Not a complete set of events, but all the ones we care about in our current tests.
 */
if (window.applicationCache && window.applicationCache.addEventListener) {
    window.applicationCache.addEventListener("checking", priv.handleAppcacheChecking, false);
    window.applicationCache.addEventListener("progress", priv.handleAppcacheProgress, false);
    window.applicationCache.addEventListener("downloading", priv.handleAppcacheDownloading, false);
    window.applicationCache.addEventListener("cached", priv.handleAppcacheCached, false);
    window.applicationCache.addEventListener("error", priv.handleAppcacheError, false);
}
