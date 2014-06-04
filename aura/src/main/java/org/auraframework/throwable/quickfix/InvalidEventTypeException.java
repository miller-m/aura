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
/**
 * Exception to 
 */
package org.auraframework.throwable.quickfix;

import org.auraframework.system.Location;

/**
 * 
 * Exception thrown when an event of incompatible type is used. Also see
 * {@link org.auraframework.def.EventType}
 */
public class InvalidEventTypeException extends AuraValidationException {

    private static final long serialVersionUID = 2571238301623320240L;

    public InvalidEventTypeException(String message, Location location) {
        super(message, location);
    }
}
