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
package org.auraframework.util.type;

import java.util.List;

import org.auraframework.util.AuraTextUtil;

public class CustomPairTypeParameterizedConverter implements Converter<String, CustomPairType> {

    @Override
    public CustomPairType convert(String value) {
        List<String> splitList = AuraTextUtil.splitSimple("$", value);
        return new CustomPairType(splitList.get(0), Integer.parseInt(splitList.get(1)));
    }

    @Override
    public Class<String> getFrom() {
        return String.class;
    }

    @Override
    public Class<CustomPairType> getTo() {
        return CustomPairType.class;
    }

    @Override
    public Class<?>[] getToParameters() {
        return new Class[] { String.class, Integer.class };
    }

}
