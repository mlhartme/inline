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
package net.oneandone.sushi.types;

import net.oneandone.sushi.types.simpletypes.BooleanType;
import net.oneandone.sushi.types.simpletypes.CharacterType;
import net.oneandone.sushi.types.simpletypes.DoubleType;
import net.oneandone.sushi.types.simpletypes.EnumType;
import net.oneandone.sushi.types.simpletypes.FloatType;
import net.oneandone.sushi.types.simpletypes.IntType;
import net.oneandone.sushi.types.simpletypes.LongType;
import net.oneandone.sushi.types.simpletypes.StringType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * A set of Types. Initially, the set consists of simple types only. Complex types
 * can be created explicitly by invoking the add method or implicitly by overriding the complex
 * method. Thus, metadata can be used as a factory for complex types.
 */
public class Schema {
    private final Map<Class<?>, Type> map;
    
    public Schema() {
        map = new HashMap<>();
        add(new StringType());
        add(new IntType());
        add(new LongType());
        add(new FloatType());
        add(new DoubleType());
        add(new BooleanType());
        add(new CharacterType());
    }

    public Type simple(Class<?> clazz) {
        Type type;
        
        if (clazz.isPrimitive()) {
            clazz = getWrapper(clazz);
        }
        type = map.get(clazz);
        if (type == null) {
            if (Enum.class.isAssignableFrom(clazz)) {
                type = EnumType.create((Class<? extends Enum>) clazz);
            } else {
                throw new IllegalStateException();
            }
            map.put(clazz, type);
        }
        return type;
    }

    public void add(Type type) {
        map.put(type.getRawType(), type);
    }
    
    //--

    private static final List<?> PRIMITIVE_TYPES = Arrays.asList(
            Void.TYPE, Boolean.TYPE, Byte.TYPE, Character.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE
    );

    private static final List<?> WRAPPER_TYPES = Arrays.asList(
            Void.class, Boolean.class, Byte.class, Character.class, Integer.class, Long.class, Float.class, Double.class
    );

    public static Class<?> getWrapper(Class<?> primitive) {
        int idx;

        idx = PRIMITIVE_TYPES.indexOf(primitive);
        if (idx == -1) {
            return null;
        } else {
            return (Class<?>) WRAPPER_TYPES.get(idx);
        }
    }
}
