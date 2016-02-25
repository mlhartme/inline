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
package net.oneandone.inline.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Function;

public class Primitive {
    protected final Class<?> raw;
    private final Function<String, ? extends Object> parser;
    public final String expected;
    private final Object defaultValue;

    public Primitive(Type raw, String expected, Object dflt, Function<String, ? extends Object> parser) {
        if (raw instanceof Class) {
            this.raw = (Class) raw;
        } else {
            try {
                this.raw = (Class) ((ParameterizedType) raw).getRawType();
            } catch (ClassCastException e) {
                throw new UnsupportedOperationException();
            }
        }
        if (this.raw.isPrimitive()) {
            throw new IllegalArgumentException(this.raw.getName());
        }
        if (this.raw.isArray()) {
            throw new IllegalArgumentException(this.raw.getName());
        }
        if (Collection.class.isAssignableFrom(this.raw)) {
            throw new IllegalArgumentException(this.raw.getName());
        }
        this.parser = parser;
        this.expected = expected;
        this.defaultValue = dflt;
    }

    public final Object parse(String str) {
        return parser.apply(str);
    };

    public Class<?> getRawType() {
        return raw;
    }

    public Object defaultValue() {
        return defaultValue;
    }

}
