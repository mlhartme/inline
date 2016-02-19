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
package net.oneandone.inline.types;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;

public abstract class Type {
    protected final Class<?> raw;
    private final Object defaultValue;

    public Type(java.lang.reflect.Type raw, Object dflt) {
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
        this.defaultValue = dflt;
    }

    /** throws an SimpleTypeException to indicate a parsing problem */
    public abstract Object parse(String str) throws ParseException;

    public Class<?> getRawType() {
        return raw;
    }

    public Object defaultValue() {
        return defaultValue;
    }

}
