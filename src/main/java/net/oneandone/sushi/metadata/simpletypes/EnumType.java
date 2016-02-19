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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class EnumType extends Type {
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
        super(clazz);
        this.values = values;
    }
    
    @Override
    public Object newInstance() {
        return values[0];
    }

    @Override
    public Object parse(String str) throws ParseException {
        StringBuilder msg;
        String name;
        
        str = normalizeEnum(str);
        msg = new StringBuilder();
        for (Enum e : values) {
            name = normalizeEnum(e.name());
            if (name.equals(str)) {
                return e;
            }
            msg.append(" '");
            msg.append(name);
            msg.append('\'');
        }
        throw new ParseException("unknown value '" + str + "', expected one of" + msg);
    }

    private static String normalizeEnum(String value) {
        value = value.toLowerCase();
        return value.replace('_', '-');
    }
}
