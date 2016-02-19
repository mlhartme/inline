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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * A set of Types. Initially, the set consists of simple types only. Complex types
 * can be created explicitly by invoking the add method or implicitly by overriding the complex
 * method. Thus, metadata can be used as a factory for complex types.
 */
public class Repository {
    private final Map<Class<?>, Type> map;
    
    public Repository() {
        map = new HashMap<>();
        add(new StringType());
        add(new IntType());
        add(new LongType());
        add(new FloatType());
        add(new DoubleType());
        add(new BooleanType());
        add(new CharacterType());
    }

    public Type get(Class<?> clazz) {
        Type type;
        
        if (clazz.isPrimitive()) {
            clazz = getWrapper(clazz);
        }
        type = map.get(clazz);
        if (type == null) {
            if (Enum.class.isAssignableFrom(clazz)) {
                type = null; // TODO EnumType.create((Class<? extends Enum>) clazz);
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

    //-- built-in types

    public static class BooleanType extends Type {
        public BooleanType() {
            super(Boolean.class, "'true' or 'false'", false, str -> {
                        str = str.toLowerCase();
                        if ("true".equals(str)) {
                            return Boolean.TRUE;
                        } else if ("false".equals(str)) {
                            return Boolean.FALSE;
                        } else {
                            throw new RuntimeException("not a boolean");
                        }
                    }
                    );
        }
    }

    public static class CharacterType extends Type {
        public CharacterType() {
            super(Character.class, "single character", (char) 0,
                    str -> {
                        if (str.length() == 1) {
                            return str.charAt(0);
                        } else {
                            throw new RuntimeException("unexpected string length: " + str.length());
                        }
                    });
        }
    }

    public static class DoubleType extends Type {
        public DoubleType() {
            super(Double.class, "double", (double) 0, Double::parseDouble);
        }
    }

    /*
    public static class EnumType extends Type {
        public static EnumType create(Class<? extends Enum> clazz) {
            return new EnumType(clazz, getValues(clazz));
        }

        public static <T extends Enum<?>> T[] getValues(Class<T> clazz) {
            Method m;

            try {
                m = clazz.getDeclaredMethod("values");
            } catch (SecurityException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            m.setAccessible(true);
            try {
                return (T[]) m.invoke(null);
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        private final Enum[] values;

        public EnumType(Class<?> clazz, Enum[] values) {
            super(clazz, expected(values), values[0]);
            this.values = values;
        }

        private static String expected(Enum[] values) {
            StringBuilder msg;
            Enum e;

            msg = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                e = values[i];
                if (i == 0) {
                    // nothing
                } else if (i == values.length - 1) {
                    msg.append(" or ");
                } else {
                    msg.append(", ");
                }
                msg.append("'");
                msg.append(normalizeEnum(e.name()));
                msg.append('\'');
            }
            return msg.toString();
        }

        @Override
        public Object parse(String str) {
            String name;

            str = normalizeEnum(str);
            for (Enum e : values) {
                name = normalizeEnum(e.name());
                if (name.equals(str)) {
                    return e;
                }
            }
            throw new RuntimeException();
        }

        private static String normalizeEnum(String value) {
            value = value.toLowerCase();
            return value.replace('_', '-');
        }
    }
*/

    public static class FloatType extends Type {
        public FloatType() {
            super(Float.class, "float number", (float) 0, Float::parseFloat);
        }
    }

    public static class IntType extends Type {
        public IntType() {
            super(Integer.class, "integer", 0, Integer::parseInt);
        }
    }

    public static class LongType extends Type {
        public LongType() {
            super(Long.class, "long integer", (long) 0, Long::parseLong);
        }
    }

    public static class StringType extends Type {
        public StringType() {
            super(String.class, "string", "", str -> str);
        }
    }
}
