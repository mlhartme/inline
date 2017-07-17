/*
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
import java.util.List;

/** Defines where to store values from the command line. */
public abstract class Target {
    private final boolean list;
    private final Primitive component;

    public Target(Repository repository, Type type) {
        ParameterizedType p;
        Type[] args;

        if (type instanceof Class) {
            this.list = false;
            this.component = repository.get((Class) type);
        } else if (type instanceof ParameterizedType) {
            p = (ParameterizedType) type;
            args = p.getActualTypeArguments();
            if (!p.getRawType().equals(List.class)) {
                throw new InvalidCliException("not a list: " + type.toString());
            }
            if (args.length != 1) {
                throw new InvalidCliException("too many type parameter: " + type.toString());
            }
            if (!(args[0] instanceof Class)) {
                throw new InvalidCliException("too much nesting: " + type.toString());
            }
            this.list = true;
            this.component = repository.get((Class) args[0]);
        } else {
            throw new InvalidCliException("unsupported type: " + type);
        }
    }

    public Target(boolean list, Primitive component) {
        this.list = list;
        this.component = component;
    }

    /** return true to set pass values before command object instantiation */
    public abstract boolean before();

    /** value is an instance of the reflection type */
    public abstract void doSet(Object dest, Object value);

    public boolean isList() {
        return list;
    }

    public boolean isBoolean() {
        return !list && (component.getRawType().equals(Boolean.TYPE) || component.getRawType().equals(Boolean.class));
    }

    public Object stringToComponent(String str) {
        return component.parse(str);
    }

    public Object defaultComponent() {
        return component.defaultValue();
    }

    public String expected() {
        return component.expected;
    }
}
