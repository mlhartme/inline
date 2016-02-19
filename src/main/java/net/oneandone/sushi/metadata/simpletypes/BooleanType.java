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

public class BooleanType extends Type {
    public BooleanType() {
        super(Boolean.class);
    }
    
    @Override
    public Object newInstance() {
        return Boolean.FALSE;
    }

    @Override
    public Object stringToValue(String str) throws SimpleTypeException {
        // TODO: because oocalc turns them to upper case
        str = str.toLowerCase();
        if ("true".equals(str)) {
            return Boolean.TRUE;
        } else if ("false".equals(str)) {
            return Boolean.FALSE;
        } else {
            throw new SimpleTypeException("expected true or false, got " + str + ".");
        }
    }
}
