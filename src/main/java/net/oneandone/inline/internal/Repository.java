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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Defines how to instantiate type from command line arguments. */
public class Repository {
    private final Map<Class<?>, Primitive> map;
    
    public Repository() {
        map = new HashMap<>();
        register(String.class, "string", "", str -> str);
        register(Integer.class, Integer.TYPE, "integer", 0, Integer::parseInt);
        register(Long.class, Long.TYPE, "long integer", (long) 0, Long::parseLong);
        register(Float.class, Float.TYPE, "float number", (float) 0, Float::parseFloat);
        register(Double.class, Double.TYPE, "double", (double) 0, Double::parseDouble);
        registerBoolean();
        register(Character.class, Character.TYPE, "single character", (char) 0,
                    str -> {
                        if (str.length() == 1) {
                            return str.charAt(0);
                        } else {
                            throw new RuntimeException("unexpected string length: " + str.length());
                        }
                    });
        register(File.class, "file name", new File("."), str -> new File(str));
        register(URL.class, "url", url("http://localhost"), Repository::url);
        register(URI.class, "uri", URI.create("http://localhost"), URI::create);
    }

    private void registerBoolean() {
        final String expected = "'true' or 'false'";
        Function<String, ? extends Object> parser;

        parser = str -> {
            str = str.toLowerCase();
            if ("true".equals(str)) {
                return Boolean.TRUE;
            } else if ("false".equals(str)) {
                return Boolean.FALSE;
            } else {
                throw new RuntimeException("not a boolean: '" + str + "'");
            }
        };

        map.put(Boolean.TYPE, new Primitive(Boolean.class /* not type! */, expected, false, parser));
        map.put(Boolean.class, new Primitive(Boolean.class, expected, null, parser));
    }

    private static URL url(String str) {
        try {
            return new URL(str);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public Primitive get(Class<?> clazz) {
        Primitive primitive;
        
        primitive = map.get(clazz);
        if (primitive == null) {
            if (!Enum.class.isAssignableFrom(clazz)) {
                throw new InvalidCliException("unknown primitive: " + clazz);
            }
            primitive = forEnum((Class) clazz);
            map.put(clazz, primitive);
        }
        return primitive;
    }

    public void register(Class<?> clazz, Class<?> primitive, String expected, Object defaultValue, Function<String, ? extends Object> parser) {
        map.put(primitive, register(clazz, expected, defaultValue, parser));
    }

    public Primitive register(Class<?> clazz, String expected, Object defaultValue, Function<String, ? extends Object> parser) {
        Primitive primitive;

        primitive = new Primitive(clazz, expected, defaultValue, parser);
        map.put(primitive.getRawType(), primitive);
        return primitive;
    }

    //--

    public static Primitive forEnum(Class<? extends Enum> clazz) {
        Enum[] values = getValues(clazz);
        return new Primitive(clazz, expected(values), values[0], str -> {
            String name;

            str = normalizeEnum(str);
            for (Enum e : values) {
                name = normalizeEnum(e.name());
                if (name.equals(str)) {
                    return e;
                }
            }
            throw new RuntimeException();
        });
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

    private static String normalizeEnum(String value) {
        value = value.toLowerCase();
        return value.replace('_', '-');
    }
}
