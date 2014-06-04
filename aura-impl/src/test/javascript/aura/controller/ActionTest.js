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
Function.RegisterNamespace("Test.Aura.Controller");

[ Fixture ]
Test.Aura.Controller.ActionTest = function() {
	// Mock the exp() function defined in Aura.js, this is originally used for exposing members using a export.js file
	Mocks.GetMock(Object.Global(), "exp", function() {
	})(function() {
		// #import aura.controller.Action
	});

	var targetNextActionId = 123;
	var mockActionId = Mocks.GetMock(Action.prototype, "nextActionId", targetNextActionId);

	[ Fixture ]
	function Constructor() {
		[ Fact ]
		function SetsStateToNew() {
			// Arrange
			var expected = "NEW";
			var target = new Action();

			// Act
			var actual = target.state;

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function SetsActionId() {
			// Arrange
			var expected = targetNextActionId;
			var target;

			// Act
			mockActionId(function() {
				target = new Action();
			});
			var actual = target.actionId;

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function IncrementsNextActionId() {
			// Arrange
			var expected = targetNextActionId + 1;
			var target;
			var actual;

			// Act
			mockActionId(function() {
				target = new Action();
				actual = Action.prototype.nextActionId;
			});

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function GetId() {

		var targetContextNum = "expectedContextNum";
		var mockContext = Mocks.GetMock(Object.Global(), "$A", {
			getContext : function() {
				return {
					getNum : function() {
						return targetContextNum;
					}
				};
			}
		});

		[ Fact ]
		function ReturnsIdIfSet() {
			// Arrange
			var expected = "expected";
			var target = new Action();
			target.id = expected;

			// Act
			var actual = target.getId();

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function ConstructsIdIfNotSet() {
			// Arrange
			var expected = String.Format("{0}.{1}", targetNextActionId, targetContextNum);
			var actual;

			// Act
			mockContext(function() {
				mockActionId(function() {
					actual = new Action().getId();
				});
			});

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function SetsConstructedIdOnAction() {
			// Arrange
			var expected = String.Format("{0}.{1}", targetNextActionId, targetContextNum);
			var target;

			// Act
			mockContext(function() {
				mockActionId(function() {
					target = new Action();
					target.getId();
				});
			});
			var actual = target.id;

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function GetNextGlobalId() {
		[ Fact ]
		function ReturnsOneIfNotSet() {
			// Arrange
			var expected = 1;
			var target = new Action();

			// Act
			var actual = target.getNextGlobalId();

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function ReturnsNextGlobalIdWhenSet() {
			// Arrange
			var expected = 123;
			var target = new Action();
			target.nextGlobalId = expected;

			// Act
			var actual = target.getNextGlobalId();

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function IncrementsIdAfterUse() {
			// Arrange
			var expected = 100;
			var target = new Action();
			target.nextGlobalId = 99;

			// Act
			target.getNextGlobalId();
			var actual = target.nextGlobalId;

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function GetDef() {
		[ Fact ]
		function ReturnsDef() {
			// Arrange
			var expected = "expected";
			var target = new Action();
			target.def = expected;

			// Act
			var actual = target.getDef();

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function SetParams() {
		[ Fact ]
		function MapsKeyInParamDefsToConfig() {
			var expected = "expected";
			var paramDefs = {
				key : 1
			};
			var target = new Action(null, null, paramDefs);
			var config = {
				key : expected
			};

			target.setParams(config);

			Assert.Equal({
				key : expected
			}, target.params);
		}

		[ Fact ]
		function ClearsPreviouslySetParamsIfMissingFromConfig() {
			var paramDefs = {
				key1 : 1,
				key2 : 2
			};
			var config = {
				key2 : "new"
			};
			var target = new Action(null, null, paramDefs);
			target.params["key1"] = "existing";

			target.setParams(config);

			Assert.Equal({
				key1 : undefined,
				key2 : "new"
			}, target.params);
		}

		[ Fact ]
		function DoesNotSetParamsWithoutDefs() {
			var paramDefs = {
				key1 : 1,
				key2 : 2
			};
			var config = {
				key1 : "new",
				key3 : "ignored"
			};
			var target = new Action(null, null, paramDefs);

			target.setParams(config);

			Assert.Equal({
				key1 : "new",
				key2 : undefined
			}, target.params);
		}
	}

	[ Fixture ]
	function GetParam() {
		[ Fact ]
		function ReturnsValueFromParamsIfKeyFound() {
			var expected = "expected";
			var paramsKey = "key";
			var target = new Action();
			target.params[paramsKey] = expected;

			var actual = target.getParam(paramsKey);

			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function ReturnsUndefinedIfKeyNotFound() {
			var paramsKey = "key";
			var target = new Action();
			target.params = {};

			var actual = target.getParam(paramsKey);

			Assert.Undefined(actual);
		}
	}

	[ Fixture ]
	function GetParams() {
		[ Fact ]
		function ReturnsParamsObject() {
			// Arrange
			var expected = "expected";
			var target = new Action();
			target.params = expected;

			// Act
			var actual = target.getParams();

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function GetComponent() {
		[ Fact ]
		function ReturnsCmpObject() {
			// Arrange
			var expected = "expected";
			var target = new Action();
			target.cmp = expected;

			// Act
			var actual = target.getComponent();

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function SetCallback() {
		var mockContext = Mocks.GetMock(Object.Global(), "$A", {
			util : {
				isFunction : function() {
					return true;
				}
			}
		});

		[ Fact ]
		function SetsCallbackWhenNameSet() {
			// Arrange
			var expectedScope = "expectedScope";
			var expectedCallback = "expectedCallback";
			var name = "SUCCESS";
			var target = new Action();

			// Act
			mockContext(function() {
				target.setCallback(expectedScope, expectedCallback, name);
			});

			// Assert
			Assert.Equal({
				"SUCCESS" : {
					s : expectedScope,
					fn : expectedCallback
				}
			}, target.callbacks);
		}

		[ Fact ]
		function ThrowsErrorWhenNameIsInvalid() {
			// Arrange
			var stubbedError = Stubs.GetMethod("msg", null);
			var name = "someInvalidName";
			var target = new Action();

			// Act
			mockContext(function() {
				$A.error = stubbedError;
				target.setCallback(null, null, name);
			});

			// Assert
			Assert.Equal("Illegal name " + name, stubbedError.Calls[0].Arguments.msg);
		}

		[ Fact ]
		function SetsAllCallbacksAndScopeWhenNameUndefined() {
			// Arrange
			var expectedScope = "expectedScope";
			var expectedCallback = "expectedCallback";
			var callbackNames = [ "SUCCESS", "ERROR", "ABORTED", "INCOMPLETE" ];
			var expected = {
				s : expectedScope,
				fn : expectedCallback
			};
			var target = new Action();

			// Act
			mockContext(function() {
				target.setCallback(expectedScope, expectedCallback);
			});

			// Assert
			Assert.Equal({
				"SUCCESS" : expected,
				"ERROR" : expected,
				"ABORTED" : expected,
				"INCOMPLETE" : expected
			}, target.callbacks);
		}

		[ Fact ]
		function SetsAllCallbacksAndScopeWhenNameAll() {
			// Arrange
			var expectedScope = "expectedScope";
			var expectedCallback = "expectedCallback";
			var callbackNames = [ "SUCCESS", "ERROR", "ABORTED", "INCOMPLETE" ];
			var expected = {
				s : expectedScope,
				fn : expectedCallback
			};
			var target = new Action();

			// Act
			mockContext(function() {
				target.setCallback(expectedScope, expectedCallback, "ALL");
			});

			// Assert
			Assert.Equal({
				"SUCCESS" : expected,
				"ERROR" : expected,
				"ABORTED" : expected,
				"INCOMPLETE" : expected
			}, target.callbacks);
		}

		[ Fact ]
		function ThrowsErrorIfCallbackNotAFunction() {
			// Arrange
			var expected = "Action callback should be a function";
			var mockContext = Mocks.GetMock(Object.Global(), "$A", {
				error : function(msg) {
					actual = msg;
				},
				util : {
					isFunction : function() {
						return false;
					}
				}
			})
			var target = new Action();
			var actual;

			// Act
			mockContext(function() {
				target.setCallback();
			});

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function WrapCallback() {
		[ Fact ]
		function SetsCallbackToCurrentCallbackThenNewCallback() {
			// Arrange
			var expectedScope = "expectedScope";
			var outerCallbackFlag = false; // Set when function passed in as param is called
			var outerCallback = function() {
				outerCallbackFlag = true;
			}
			var target = new Action();
			target.getState = function() {
				return "STATE";
			};
			target.callbacks = {
				"STATE" : {
					"fn" : function(scope, callback) {
						if (outerCallbackFlag) {
							Assert.Fail("New callback called before current callback");
						}
					}
				}
			};
			target.setCallback = function(scope, func) {
				func.call(scope); // Call what the new callback is set as to test logic inside
			}

			// Act
			target.wrapCallback(null, outerCallback);

			// Assert
			Assert.True(outerCallbackFlag);
		}
	}

	[ Fixture ]
	function RunDeprecated() {
		[ Fact ]
		function AssertsIsClientAction() {
			// Arrange
			var expected = "expected";
			var mockAssert = Mocks.GetMock(Object.Global(), "$A", {
				assert : function(param) {
					actual = param;
				}
			});
			var def = {
				isClientAction : function() {
					return expected;
				}
			};
			var cmp = {
				getDef : function() {
					return {
						getHelper : function() {
						}
					}
				}
			};
			var meth = {
				call : function() {
				}
			};
			var target = new Action();
			target.def = def;
			target.meth = meth;
			target.cmp = cmp;
			var actual = null;

			// Act
			mockAssert(function() {
				target.runDeprecated();
			})

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function LogsFailMessageOnException() {
			// Arrange
			var expectedName = "expectedName";
			var expectedQualifiedName = "expectedQN";
			var expected = "Action failed: " + expectedQualifiedName + " -> " + expectedName;
			var mockAssert = Mocks.GetMock(Object.Global(), "$A", {
				assert : function(param) {
				},
				log : function(msg) {
					actual = msg;
				}
			});
			var def = {
				isClientAction : function() {
				}
			};
			var cmp = {
				getDef : function() {
					return {
						getDescriptor : function() {
							return {
								getQualifiedName : function() {
									return expectedQualifiedName;
								}
							}
						}
					}
				}
			};
			var target = new Action();
			target.def = def;
			target.cmp = cmp;
			target.getDef = function() {
				return {
					getName : function() {
						return expectedName;
					}
				}
			}
			var actual = null;

			// Act
			mockAssert(function() {
				target.runDeprecated();
			})

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function SetsStateToSuccess() {
			// Arrange
			var expectedState = "SUCCESS";
			var mockAssert = Mocks.GetMock(Object.Global(), "$A", {
				assert : function(param) {
				}
			});
			var def = {
				isClientAction : function() {
				}
			};
			var cmp = {
				getDef : function() {
					return {
						getHelper : function() {
						}
					}
				}
			};
			var meth = {
				call : function() {
				}
			};
			var target = new Action();
			target.def = def;
			target.meth = meth;
			target.cmp = cmp;

			// Act
			mockAssert(function() {
				target.runDeprecated();
			})

			// Assert
			Assert.Equal(expectedState, target.state);
		}

		[ Fact ]
		function SetsStateToFailureOnException() {
			// Arrange
			var expectedState = "FAILURE";
			var mockAssert = Mocks.GetMock(Object.Global(), "$A", {
				assert : function(param) {
				},
				log : function() {
				}
			});
			var def = {
				isClientAction : function() {
				}
			};
			var cmp = {
				getDef : function() {
					return {
						getDescriptor : function() {
							return {
								getQualifiedName : function() {
								}
							}
						}
					}
				}
			};
			var target = new Action();
			target.def = def;
			target.cmp = cmp;
			target.getDef = function() {
				return {
					getName : function() {
					}
				}
			}

			// Act
			mockAssert(function() {
				target.runDeprecated();
			})

			// Assert
			Assert.Equal(expectedState, target.state);
		}
	}

	[ Fixture ]
	function GetState() {
		[ Fact ]
		function ReturnsState() {
			// Arrange
			var expected = "expected";
			var target = new Action();
			target.state = expected;

			// Act
			var actual = target.getState();

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function GetReturnValue() {
		[ Fact ]
		function ReturnsReturnValue() {
			// Arrange
			var expected = "expected";
			var target = new Action();
			target.returnValue = expected;

			// Act
			var actual = target.getReturnValue();

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function GetError() {
		[ Fact ]
		function ReturnsError() {
			// Arrange
			var expected = "expected";
			var target = new Action();
			target.error = expected;

			// Act
			var actual = target.getError();

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function IsBackground() {
		[ Fact ]
		function ReturnsTrueIfBackgroundSet() {
			// Arrange
			var target = new Action();
			target.background = true;

			// Act
			var actual = target.isBackground();

			// Assert
			Assert.True(actual);
		}

		[ Fact ]
		function ReturnsFalseIfBackgroundNotSet() {
			// Arrange
			var target = new Action();

			// Act
			var actual = target.isBackground();

			// Assert
			Assert.False(actual);
		}

		[ Fact ]
		function ReturnsFalseIfBackgroundNotTrue() {
			// Arrange
			var target = new Action();
			target.background = "true";

			// Act
			var actual = target.isBackground();

			// Assert
			Assert.False(actual);
		}
	}

	[ Fixture ]
	function SetBackground() {
		[ Fact ]
		function SetsBackgroundToTrue() {
			// Arrange
			var target = new Action();

			// Act
			target.setBackground();
			var actual = target.background;

			// Assert
			Assert.True(actual);
		}

		[ Fact ]
		function CannotSetBackgroundToFalse() {
			var target = new Action();
			target.background = true;

			target.setBackground(false);
			var actual = target.background;

			Assert.True(actual);
		}
	}

	[ Fixture ]
	function RunAfter() {
		[ Fact ]
		function AssertsIsServerAction() {
			// Arrange
			var expected = "expected";
			var expectedReturn = "expectedReturn";
			var mockAssert = Mocks.GetMock(Object.Global(), "$A", {
				assert : function(param) {
					if (param === expectedReturn) {
						actual = expected;
					}
				},
				clientService : {
					enqueueAction : function() {
					}
				}
			});
			var target = new Action();
			var action = {
				def : {
					isServerAction : function() {
						return expectedReturn;
					}
				}
			};
			var actual = null;

			// Act
			mockAssert(function() {
				target.runAfter(action);
			})

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function AddsActionParamToQueue() {
			// Arrange
			var expectedReturn = "expectedReturn";
			var mockAssert = Mocks.GetMock(Object.Global(), "$A", {
				assert : function(param) {
				},
				clientService : {
					enqueueAction : function(param) {
						actual = param;
					}
				}
			});
			var target = new Action();
			var action = {
				def : {
					isServerAction : function() {
					}
				}
			};
			var actual = null;

			// Act
			mockAssert(function() {
				target.runAfter(action);
			})

			// Assert
			Assert.Equal(action, actual);
		}

		[ Fact ]
		function ThrowsIfActionIsNotServerAction() {
			// Arrange
			var expected = "RunAfter() cannot be called on a client action. Use run() on a client action instead.";
			var mockAssert = Mocks.GetMock(Object.Global(), "$A", {
				assert : function(condition, message) {
					if (!condition) {
						var error = new Error(message);
						throw error;
					}
				}
			});
			var target = new Action();
			var action = {
				def : {
					isServerAction : function() {
						return false;
					}
				}
			};
			var actual = null;

			// Act
			mockAssert(function() {
				actual = Record.Exception(function() {
					target.runAfter(action);
				})
			});

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function GetStored() {
		[ Fact ]
		function NullIfNotSuccessfull() {
			// Arrange
			var target = new Action();
			target.storable = true;
			target.returnValue = "NONE";
			target.state = "FAILURE";
			target.responseState = "FAILURE";

			// Act
			var stored = target.getStored("bogus");

			// Assert
			Assert.Equal(null, stored);
		}

		[ Fact ]
		function NullIfNotStorable() {
			// Arrange
			var target = new Action();
			target.storable = false;
			target.returnValue = "NONE";
			target.state = "SUCCESS";
			target.responseState = "SUCCESS";

			// Act
			var stored = target.getStored("bogus");

			// Assert
			Assert.Equal(null, stored);
		}

		[ Fact ]
		function ValuesFromResponse() {
			// Arrange
			var target = new Action();
			target.storable = true;
			target.returnValue = "NONE";
			target.state = "SUCCESS";
			target.responseState = "SUCCESS";
			target.components = {};

			// Act
			var stored = target.getStored("bogus");

			// Assert
			Assert.Equal("NONE", stored["returnValue"]);
			Assert.Equal({}, stored["components"]);
			Assert.Equal("SUCCESS", stored["state"]);
			Assert.Equal("bogus", stored["storage"]["name"]);
			// time is harder.
		}
	}

	[ Fixture ]
	function FinishAction() {

		[ Fact ]
		function CallsActionCallbackIfCmpIsValid() {
			// Arrange
			var target = new Action();
			target.sanitizeStoredResponse = function() {
			};
			delete target.originalResponse;
			target.getState = function() {
				return "NOTERRORSTATE"
			};
			target.cmp = {
				isValid : function() {
					return true;
				}
			};
			target.callbacks = {
				"NOTERRORSTATE" : {
					"fn" : function() {
						actual = true;
					}
				}
			};
			target.getStorage = function() {
				return false;
			}
			var actual = false;

			// Act
			target.finishAction({
				setCurrentAction : function() {
				}
			});

			// Assert
			Assert.True(actual);
		}

		[ Fact ]
		function CallsCompleteGroups() {
			var target = new Action();
			target.completeGroups = Stubs.GetMethod(null);

			var error = Record.Exception(function() {
				target.finishAction({
					setCurrentAction : function() {
					}
				});
			})

			Assert.Equal([ {
				Arguments : {},
				ReturnValue : null
			} ], target.completeGroups.Calls);
			Assert.Null(error);
		}

		[ Fact ]
		function CallsCompleteGroupsEvenOnErrors() {
			var target = new Action();
			target.completeGroups = Stubs.GetMethod(null);
			target.components = "something";

			var error = Record.Exception(function() {
				target.finishAction({
					setCurrentAction : function() {
					},
					joinComponentConfigs : function() {
						throw new Error("intentional");
					}
				});
			});

			Assert.Equal([ {
				Arguments : {},
				ReturnValue : null
			} ], target.completeGroups.Calls);
			Assert.Equal("intentional", error);
		}
	}

	[ Fixture ]
	function SetAbortable() {
		[ Fact ]
		function SetsAbortableToTrue() {
			// Arrange
			var target = new Action();

			// Act
			target.setAbortable();

			// Assert
			Assert.True(target.abortable);
		}
	}

	[ Fixture ]
	function IsAbortable() {
		[ Fact ]
		function ReturnsAbortableIfSet() {
			// Arrange
			var expected = "expected";
			var target = new Action();
			target.abortable = expected;

			// Act
			var actual = target.isAbortable();

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function ReturnsFalseIfAbortableNotSet() {
			// Arrange
			var target = new Action();
			target.abortable = undefined;

			// Act
			var actual = target.isAbortable();

			// Assert
			Assert.False(actual);
		}
	}

	[ Fixture ]
	function SetExclusive() {
		[ Fact ]
		function SetsExclusiveTrueIfParamUndefined() {
			// Arrange
			var target = new Action();

			// Act
			target.setExclusive(undefined);

			// Assert
			Assert.True(target.exclusive);
		}

		[ Fact ]
		function SetsExclusiveToParamIfDefined() {
			// Arrange
			var expected = "expected";
			var target = new Action();

			// Act
			target.setExclusive(expected);
			var actual = target.exclusive;

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function IsExclusive() {
		[ Fact ]
		function ReturnsExclusiveIfSet() {
			// Arrange
			var expected = "expected";
			var target = new Action();
			target.exclusive = expected;

			// Act
			var actual = target.isExclusive();

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function ReturnsFalseIfExclusiveNotSet() {
			// Arrange
			var target = new Action();
			target.exclusive = undefined;

			// Act
			var actual = target.isExclusive();

			// Assert
			Assert.False(actual);
		}
	}

	[ Fixture ]
	function SetStorable() {
		[ Fact ]
		function SetsStorableToTrue() {
			// Arrange
			var mockAssert = Mocks.GetMock(Object.Global(), "$A", {
				assert : function() {
				}
			});
			var target = new Action();
			target.def = {
				isServerAction : function() {
				}
			};
			target.setAbortable = function() {
			};
			var actual;

			// Act
			mockAssert(function() {
				target.setStorable();
				actual = target.storable;
			})

			// Assert
			Assert.True(actual);
		}

		[ Fact ]
		function SetsStorableConfigToParam() {
			// Arrange
			var expected = "expected";
			var mockAssert = Mocks.GetMock(Object.Global(), "$A", {
				assert : function() {
				}
			});
			var target = new Action();
			target.def = {
				isServerAction : function() {
				}
			};
			target.setAbortable = function() {
			};
			var actual;

			// Act
			mockAssert(function() {
				target.setStorable(expected);
				actual = target.storableConfig;
			})

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function SetsStorableCallsSetAbortable() {
			// Arrange
			var expected = "expected";
			var mockAssert = Mocks.GetMock(Object.Global(), "$A", {
				assert : function() {
				}
			});
			var target = new Action();
			target.def = {
				isServerAction : function() {
				}
			};
			target.setAbortable = function() {
				actual = expected;
			};
			var actual = null;

			// Act
			mockAssert(function() {
				target.setStorable();
			})

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function AssertsDefIsServerAction() {
			// Arrange
			var expected = "expected";
			var expectedReturn = "expectedReturn";
			var mockAssert = Mocks.GetMock(Object.Global(), "$A", {
				assert : function(param) {
					if (param === expectedReturn) {
						actual = expected;
					}
				}
			});
			var target = new Action();
			target.def = {
				isServerAction : function() {
					return expectedReturn;
				}
			};
			target.setAbortable = function() {
			};
			var actual = null;

			// Act
			mockAssert(function() {
				target.setStorable();
			})

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function IsStorable() {
		[ Fact ]
		function ReturnsFalseWhenIgnoreExistingFlagSet() {
			// Arrange
			var target = new Action();
			target.storableConfig = {
				ignoreExisting : true
			};
			target._isStorable = function() {
				return true;
			}

			// Act
			var ret = target.isStorable();

			// Assert
			Assert.False(ret);
		}

		[ Fact ]
		function ReturnsFalseWhen_IsStorableFalse() {
			// Arrange
			var target = new Action();
			target._isStorable = function() {
				return false;
			}

			// Act
			var ret = target.isStorable();

			// Assert
			Assert.False(ret);
		}

		[ Fact ]
		function ReturnsTrueWhen_IsStorableTrue() {
			// Arrange
			var target = new Action();
			target._isStorable = function() {
				return true;
			}

			// Act
			var ret = target.isStorable();

			// Assert
			Assert.True(ret);
		}
	}

	[ Fixture ]
	function _IsStorable() {
		[ Fact ]
		function ReturnsStorableIfSet() {
			// Arrange
			var expected = "expected";
			var target = new Action();
			target.storable = expected;

			// Act
			var actual = target._isStorable();

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function ReturnsFalseIfStorableNotSet() {
			// Arrange
			var target = new Action();
			target.storable = undefined;

			// Act
			var actual = target._isStorable();

			// Assert
			Assert.False(actual);
		}
	}

	[ Fixture ]
	function getStorageKey() {
		[ Fact ]
		function ReturnsKeyAsDescriptorAndEncodedParams() {
			// Arrange
			var expectedEncode = "encodedString";
			var expectedDescriptor = "expectedDescriptor";
			var expected = expectedDescriptor + ":" + expectedEncode;
			var mockContext = Mocks.GetMock(Object.Global(), "$A", {
				util : {
					json : {
						encode : function() {
							return expectedEncode;
						}
					}
				}
			});
			var target = new Action();
			target.getParams = function() {
			};
			target.getDef = function() {
				return {
					getDescriptor : function() {
						return {
							toString : function() {
								return expectedDescriptor;
							}
						}
					}
				}
			};

			// Act
			mockContext(function() {
				actual = target.getStorageKey();
			});

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function IsFromStorage() {
		var mockContext = Mocks.GetMock(Object.Global(), "$A", {
			util : {
				isUndefinedOrNull : function(storage) {
					return storage === undefined || storage === null;
				}
			}
		});
		[ Fact ]
		function ReturnsTrueIfStorageSet() {
			var target = new Action();
			target.storage = {};
			var actual = null;

			mockContext(function() {
				actual = target.isFromStorage();
			});

			Assert.True(actual);
		}

		[ Fact ]
		function ReturnsFalseIfStorageNotSet() {
			var target = new Action();
			delete target.storage;
			var actual = null;

			mockContext(function() {
				actual = target.isFromStorage();
			});

			Assert.False(actual);
		}

		[ Fact ]
		function ReturnsFalseIfStorageNull() {
			var target = new Action();
			target.storage = null;
			var actual = null;

			mockContext(function() {
				actual = target.isFromStorage();
			});

			Assert.False(actual);
		}
	}

	[ Fixture ]
	function SetChained() {
		[ Fact ]
		function SetsChainedTrue() {
			// Arrange
			var target = new Action();
			var mockContext = Mocks.GetMock(Object.Global(), "$A", {
				enqueueAction : function() {
				}
			});

			// Act
			mockContext(function() {
				target.setChained();
			})

			// Assert
			Assert.True(target.chained);
		}

		[ Fact ]
		function ChainsCurrentAction() {
			// Arrange
			var target = new Action();
			var mockContext = Mocks.GetMock(Object.Global(), "$A", {
				enqueueAction : function(param) {
					actual = param;
				}
			});
			var actual = null;

			// Act
			mockContext(function() {
				target.setChained();
			})

			// Assert
			Assert.Equal(target, actual);
		}
	}

	[ Fixture ]
	function IsChained() {
		[ Fact ]
		function ReturnsChainedIfSet() {
			// Arrange
			var expected = "expected";
			var target = new Action();
			target.chained = expected;

			// Act
			var actual = target.isChained();

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function ReturnsFalseIfStorableNotSet() {
			// Arrange
			var target = new Action();
			target.chained = undefined;

			// Act
			var actual = target.isChained();

			// Assert
			Assert.False(actual);
		}
	}

	[ Fixture ]
	function ToJSON() {
		[ Fact ]
		function ReturnsMapOfIdDescriptorAndParams() {
			// Arrange
			var expectedId = "expectedId";
			var expectedDescriptor = "expectedDescriptor";
			var expectedParams = "expectedParams";
			var expected = {
				"id" : expectedId,
				"descriptor" : expectedDescriptor,
				"params" : expectedParams
			};
			var target = new Action();
			target.getId = function() {
				return expectedId;
			}
			target.getParams = function() {
				return expectedParams;
			}
			target.getDef = function() {
				return {
					getDescriptor : function() {
						return expectedDescriptor;
					}
				}
			}

			// Act
			var actual = target.toJSON();

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	// [Fixture]
	// function GetRefreshAction(){
	// }

	[ Fixture ]
	function SanitizeStoredResponse() {
		[ Fact ]
		function ChangesGlobalIdOfComponent() {
			// Arrange
			var suffix = "newSuffix";
			var expectedNewId = "globalId:" + suffix;
			var target = new Action();
			target.getId = function() {
				return suffix;
			}
			var response = {
				"components" : {
					"globalId:originalSuffix" : {
						"globalId" : "originalId"
					}
				}
			}

			// Act
			target.sanitizeStoredResponse(response);

			// Assert
			Assert.True(expectedNewId in response["components"]);
		}

		[ Fact ]
		function AddsKeyNamedGlobalIdWithNewGlobalIdAsValueToResponse() {
			// Arrange
			var suffix = "newSuffix";
			var expectedNewId = "globalId:" + suffix;
			var target = new Action();
			target.getId = function() {
				return suffix;
			}
			var response = {
				"components" : {
					"globalId:originalSuffix" : {
						"globalId" : "originalId"
					}
				}
			}

			// Act
			target.sanitizeStoredResponse(response);
			var actual = response["components"][expectedNewId]["globalId"];

			// Assert
			Assert.Equal(expectedNewId, actual);
		}

		[ Fact ]
		function ChangesReturnValueGlobalIdIfSet() {
			// Arrange
			var suffix = "newSuffix";
			var expectedNewId = "globalId:" + suffix;
			var target = new Action();
			target.getId = function() {
				return suffix;
			}
			var response = {
				"components" : {},
				"returnValue" : {
					"globalId" : "globalId:origSuffix"
				}
			}

			// Act
			target.sanitizeStoredResponse(response);
			var actual = response["returnValue"]["globalId"];

			// Assert
			Assert.Equal(expectedNewId, actual);
		}
	}

	[ Fixture ]
	function GetStorage() {
		[ Fact ]
		function ReturnsStorageServiceGetStorage() {
			// Arrange
			var target = new Action();
			var mockStorageService = Mocks.GetMock(Object.Global(), "$A", {
				storageService : {
					getStorage : function(param) {
						return param === "actions";
					}
				}
			});
			var actual = false;

			// Act
			mockStorageService(function() {
				actual = target.getStorage();
			})

			// Assert
			Assert.True(actual);
		}
	}

	[ Fixture ]
	function ParseAndFireEvent() {
		[ Fact ]
		function CallsClientServiceWhenEventNotFoundByDescriptor() {
			// Arrange
			var expected = "expected";
			var mockClientService = Mocks.GetMock(Object.Global(), "$A", {
				clientService : {
					parseAndFireEvent : function() {
						actual = expected;
					}
				}
			});
			var target = new Action();
			target.getComponent = function() {
				return {
					getEventByDescriptor : function() {
						return null;
					}
				}
			}
			var actual = null;

			// Act
			mockClientService(function() {
				target.parseAndFireEvent("");
			})

			// Assert
			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function FiresEventWhenEventFoundByDescriptor() {
			// Arrange
			var expected = "expected";
			var evt = {
				fire : function() {
					actual = expected;
				}
			}
			var target = new Action();
			target.getComponent = function() {
				return {
					getEventByDescriptor : function() {
						return evt;
					}
				}
			}
			var actual = null;

			// Act
			target.parseAndFireEvent("");

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function FireRefreshEvent() {
		[ Fact ]
		function FiresRefreshEventIfImplementsRefreshObserver() {
			// Arrange
			var expected = "expected";
			var target = new Action();
			target.cmp = {
				isValid : function() {
					return true;
				},
				isInstanceOf : function(param) {
					return param === "auraStorage:refreshObserver";
				},
				getEvent : function() {
					return {
						setParams : function() {
							return {
								fire : function() {
									actual = expected;
								}
							}
						}
					}
				}
			}
			var actual = null;

			// Act
			target.fireRefreshEvent("refreshBegin");

			// Assert
			Assert.Equal(expected, actual);
		}
	}

	[ Fixture ]
	function AddCallbackGroup() {
		var MockGroup = function() {
			this.completeAction = Stubs.GetMethod("action", null);
		}

		[Fact]
		function AddsGroupIfNew() {
			var expected = "expected";
			var target = new Action();
			target.state = "NEW";

			target.addCallbackGroup(expected);
			var actual = target.groups;

			Assert.Equal([ expected ], actual);
		}

		[ Fact ]
		function DoesNotAddGroupIfNotNew() {
			var expected = [];
			var target = new Action();
			target.state = "new";
			var group = new MockGroup();

			target.addCallbackGroup(group);
			var actual = target.groups;

			Assert.Equal(expected, actual);
		}

		[ Fact ]
		function CallsCompleteActionOnGroupIfNotNew() {
			var target = new Action();
			target.state = "new";
			var group = new MockGroup();

			target.addCallbackGroup(group);

			Assert.Equal([ {
				Arguments : {
					action : target
				},
				ReturnValue : null
			} ], group.completeAction.Calls);
		}

		[ Fact ]
		function DoesNotCallCompleteActionOnGroupIfNew() {
			var target = new Action();
			target.state = "NEW";
			var group = new MockGroup();

			target.addCallbackGroup(group);

			Assert.Equal(0, group.completeAction.Calls.length);
		}
	}

	[ Fixture ]
	function CompleteGroups() {
		var MockGroup = function() {
			this.completeAction = Stubs.GetMethod("action", null);
		}

		[Fact]
		function SetsGroupsEmpty() {
			var target = new Action();
			var group1 = new MockGroup();
			var group2 = new MockGroup();
			target.groups = [ group1, group2 ];

			target.completeGroups();

			Assert.Equal([], target.groups);
		}

		[ Fact ]
		function CallsCompleteActionOnEachGroup() {
			var target = new Action();
			var group1 = new MockGroup();
			var group2 = new MockGroup();
			target.groups = [ group1, group2 ];

			target.completeGroups();

			Assert.Equal([ {
				Arguments : {
					action : target
				},
				ReturnValue : null
			} ], group1.completeAction.Calls);
			Assert.Equal([ {
				Arguments : {
					action : target
				},
				ReturnValue : null
			} ], group2.completeAction.Calls);
		}
	}

	[ Fixture ]
	function Abort() {
		[ Fact ]
		function SetsStateToAborted() {
			var target = new Action();

			target.abort();

			Assert.Equal("ABORTED", target.state);
		}

		[ Fact ]
		function CallsCompleteGroups() {
			var target = new Action();
			target.completeGroups = Stubs.GetMethod(null);

			target.abort();

			Assert.Equal([ {
				Arguments : {},
				ReturnValue : null
			} ], target.completeGroups.Calls);
		}
	}
}
