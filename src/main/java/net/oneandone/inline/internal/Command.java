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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Command {
    public static Command create(ContextBuilder builder, String name, Method method) {
        Class<?> returnType;

        if (Modifier.isStatic(method.getModifiers())) {
            throw new InvalidCliException(method + ": static not allowed");
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new InvalidCliException(method + ": public expected");
        }
        if (method.getParameterTypes().length != 0) {
            throw new InvalidCliException(method + ": unexpected arguments");
        }
        returnType = method.getReturnType();
        if (Void.TYPE.equals(returnType) || Integer.TYPE.equals(returnType)) {
            return new Command(builder, name, method);
        } else {
            throw new InvalidCliException("unsupported return type: " + returnType);
        }
    }

    //--

    private final ContextBuilder builder;
    private final String name;
    private final Method method;

    public Command(ContextBuilder builder, String name, Method method) {
        this.builder = builder;
        this.name = name;
        this.method = method;
    }

    public ContextBuilder getBuilder() {
        return builder;
    }

    public String getName() {
        return name;
    }

    public int run(Object obj) throws Throwable {
        Object result;
        
        try {
            result = method.invoke(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        if (result instanceof Integer) {
            return (Integer) result;
        } else {
            return 0;
        }
    }
}
