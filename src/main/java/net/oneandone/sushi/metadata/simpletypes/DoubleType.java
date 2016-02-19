/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.sushi.metadata.simpletypes;

import net.oneandone.sushi.metadata.Type;
import net.oneandone.sushi.metadata.SimpleTypeException;

public class DoubleType extends Type {
    public DoubleType() {
        super(Double.class);
    }

    @Override
    public Object newInstance() {
        return (double) 0;
    }

    @Override
    public Object stringToValue(String str) throws SimpleTypeException {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new SimpleTypeException("number expected, got '" + str + "'");
        }            
    }
}
