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
package org.auraframework.throwable.quickfix;

import org.auraframework.system.Location;

/**
 * @since 0.0.199
 */
public class StyleParserException extends AuraValidationException {

    private static final long serialVersionUID = -4506420478822192802L;

    public StyleParserException(String message, Location l, AuraQuickFix... quickFixes) {
        super(message, l, quickFixes);
    }

    public StyleParserException(String message, Location l) {
        this(message, l, (AuraQuickFix[]) null);
    }

}
