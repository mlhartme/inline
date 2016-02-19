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
import net.oneandone.sushi.metadata.ParseException;

public class FloatType extends Type {
    public FloatType() {
        super(Float.class);
    }
    
    @Override
    public Object newInstance() {
        return 0;
    }

    @Override
    public Object parse(String str) throws ParseException {
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            throw new ParseException("number expected, got '" + str + "'");
        }            
    }
}
