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
package net.oneandone.inline.parser;

import net.oneandone.inline.ArgumentException;
import net.oneandone.inline.types.Repository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public class TargetMethod extends Target {
    public static Target create(boolean iterated, Repository schema, Method method) {
        Parameter[] formals;
        Type type;

        if (Modifier.isStatic(method.getModifiers())) {
            throw new InvalidCliException(method + ": static not allowed");
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new InvalidCliException(method + ": public expected");
        }
        formals = method.getParameters();
        if (formals.length != 1) {
            throw new InvalidCliException("1 argument expected");
        }
        type = formals[0].getParameterizedType();
        if (iterated) {
            return new TargetMethodIterated(true, schema.get((Class) type), method);
        } else {
            return new TargetMethod(schema, type, method);
        }
    }
    
    //--

    private final Method method;
    
    public TargetMethod(Repository schema, Type type, Method method) {
        super(schema, type);
        this.method = method;
    }

    public boolean before() {
        return false;
    }

    @Override
    public void doSet(Object dest, Object value) {
        Throwable cause;
        
        try {
            method.invoke(dest, value);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(value + ":" + value.getClass(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            if (cause instanceof ArgumentException) {
                throw (ArgumentException) cause;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException("unexpected exception" , cause);
        }
    }
}
