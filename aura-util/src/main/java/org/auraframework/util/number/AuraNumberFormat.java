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
package org.auraframework.util.number;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

public class AuraNumberFormat {

    /**
     * Ensures strict parsing of a number. Throws ParseException if the input
     * number is not fully consumed.
     */
    public static Number parseStrict(String input, NumberFormat numberFormat) throws ParseException {
        if (input == null) {
            throw new ParseException("Input number is null", 0);
        }

        ParsePosition parsePosition = new ParsePosition(0);
        Number number = numberFormat.parse(input, parsePosition);
        if (number == null || parsePosition.getIndex() == 0) {
            throw new ParseException("Unparseable number: \"" + input + "\"", parsePosition.getErrorIndex());
        }

        if (parsePosition.getIndex() < input.length()) {
            throw new ParseException("Unparseable number: \"" + input + "\"", parsePosition.getIndex());
        }

        return number;
    }
}
