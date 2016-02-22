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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Factory for properly initialized the context object */
public class ContextBuilder {
    public static boolean isOption(String arg) {
        return arg.length() > 1 && arg.startsWith("-");
    }

    //--

    private final Context context;
    private final ContextBuilder parent;
    private final ContextFactory factory;
    private final Map<String, Argument> options;
    private final List<Argument> values;

    public ContextBuilder(Context context, ContextBuilder parent, Object commandInstance) {
        this(context, parent, new IdentityContextFactory(commandInstance));
    }

    public ContextBuilder(Context context, ContextBuilder parent, Constructor<?> constructor, Object[] constructorActuals, List<Argument> arguments) {
        this(context, parent, new ConstructorContextFactory(constructor, constructorActuals, arguments));
    }

    private ContextBuilder(Context context, ContextBuilder parent, ContextFactory factory) {
        this.context = context;
        this.parent = parent;
        this.factory = factory;
        this.options = new HashMap<>();
        this.values = new ArrayList<>();
    }

    public void addArgument(Argument arg) {
        Source source;
        String name;

        source = arg.source;
        if (source.option) {
            name = source.getName();
            if (options.put(name, arg) != null) {
                throw new InvalidCliException("duplicate option: " + name);
            }
        } else {
            values.add(arg);
        }
    }

    //--

    /** Convenience for Testing */
    public Object run(String ... args) throws Throwable {
        return run(Arrays.asList(args));
    }

    /** @return Target */
    public Object run(List<String> args) throws Throwable {
        Actuals actuals;
        Map<String, Argument> allOptions;
        List<Argument> allValues;

        actuals = new Actuals();
        define(actuals);
        allOptions = new HashMap<>();
        addOptions(allOptions);
        allValues = new ArrayList<>();
        addValues(allValues);
        actuals.fill(args, allOptions, allValues);
        return instantiate(actuals, new HashMap<>());
    }

    private Object instantiate(Actuals actuals, Map<Context, Object> instantiatedContexts) throws Throwable {
        Object obj;

        if (parent != null) {
            parent.instantiate(actuals, instantiatedContexts);
        }
        actuals.save(context, null);
        obj = factory.newInstance(instantiatedContexts);
        instantiatedContexts.put(context, obj);
        actuals.save(context, obj);
        return obj;
    }

    private void define(Actuals result) {
        if (parent != null) {
            parent.define(result);
        }
        result.defineAll(options.values());
        result.defineAll(values);
    }

    private void addOptions(Map<String, Argument> result) {
        if (parent != null) {
            parent.addOptions(result);
        }
        result.putAll(options);
    }

    private void addValues(List<Argument> result) {
        if (parent != null) {
            parent.addValues(result);
        }
        result.addAll(values);
    }

    //--

    public static abstract class ContextFactory {
        private final List<Argument> arguments;

        public ContextFactory(List<Argument> arguments) {
            this.arguments = arguments;
        }

        public abstract Object newInstance(Map<Context, Object> instantiatedContexts) throws Throwable;
    }

    public static class IdentityContextFactory extends ContextFactory {
        private final Object instance;

        public IdentityContextFactory(Object instance) {
            super(new ArrayList<>());
            this.instance = instance;
        }

        @Override
        public Object newInstance(Map<Context, Object> instantiatedContexts) throws Throwable {
            return instance;
        }
    }

    public static class ConstructorContextFactory extends ContextFactory {
        private final Constructor<?> constructor;
        private final Object[] constructorActuals;

        public ConstructorContextFactory(Constructor<?> constructor, Object[] constructorActuals, List<Argument> arguments) {
            super(arguments);
            this.constructor = constructor;
            this.constructorActuals = constructorActuals;
        }

        @Override
        public Object newInstance(Map<Context, Object> instantiatedContexts) throws Throwable {
            Object instance;

            for (int i = 0, max = constructorActuals.length; i < max; i++) {
                if (constructorActuals[i] instanceof Context) {
                    instance = instantiatedContexts.get(constructorActuals[i]);
                    if (instance == null) {
                        throw new IllegalStateException();
                    }
                    constructorActuals[i] = instance;
                }
            }
            try {
                instance = constructor.newInstance(constructorActuals);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("TODO", e);
            }
            return instance;
        }

    }
}
