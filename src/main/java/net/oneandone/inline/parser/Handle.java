package net.oneandone.inline.parser;

import net.oneandone.inline.types.Repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reference to a Class or an Instance. A ContextFactory factory. */
public abstract class Handle {
    public static Handle create(Context parent, Object handle) {
        if (handle instanceof Class<?>) {
            return new ClassHandle((Class) handle);
        } else if (handle instanceof String) {
            return FactoryHandle.doCreate(parent, (String) handle);
        } else {
            return new InstanceHandle(handle);
        }
    }

    public abstract Class<?> clazz();
    public abstract ExceptionHandler exceptionHandler();
    public abstract ContextFactory compile(Context context, Repository schema, List<Source> constructorSources);

    public String name() {
        return clazz().toString();
    }

    //--

    public static class InstanceHandle extends Handle {
        private final Object instance;

        public InstanceHandle(Object instance) {
            this.instance = instance;
        }

        public ExceptionHandler exceptionHandler() {
            if (instance instanceof ExceptionHandler) {
                return (ExceptionHandler) instance;
            } else {
                return null;
            }
        }

        public Class<?> clazz() {
            return instance.getClass();
        }

        public ContextFactory compile(Context context, Repository schema, List<Source> constructorSources) {
            if (!constructorSources.isEmpty()) {
                throw new InvalidCliException("cannot apply constructor argument to an instance");
            }
            return new IdentityContextFactory(instance);
        }
    }

    public static class FactoryHandle extends Handle {
        public static Handle doCreate(Context parent, String handle) {
            int idx;
            String ctx;

            idx = handle.indexOf('.');
            if (idx == -1) {
                throw new InvalidCliException("invalid factory handle: " + handle);
            }
            ctx = handle.substring(0, idx);
            while (parent != null) {
                if (parent.name.equals(ctx)) {
                    return new FactoryHandle(getMethod(parent, ctx.substring(idx + 1)));
                }
                parent = parent.parent;
            }
            throw new InvalidCliException("context not found: " + ctx);
        }

        private static Method getMethod(Context context, String name) {
            Class<?> clazz;
            Method result;

            result = null;
            clazz = context.handle.clazz();
            for (Method method : clazz.getMethods()) {
                if (name.equals(method.getName())) {
                    if (result != null) {
                        throw new InvalidCliException("method ambiguous: " + result + " vs " + method);
                    }
                    result = method;
                }
            }
            if (result == null) {
                throw new InvalidCliException("method not found: " + clazz.getName() + "." + name + "(...)");
            }
            return result;
        }

        private final Method method;

        public FactoryHandle(Method method) {
            this.method = method;
        }

        @Override
        public Class<?> clazz() {
            return method.getReturnType();
        }

        @Override
        public ExceptionHandler exceptionHandler() {
            return null;
        }

        @Override
        public ContextFactory compile(Context context, Repository schema, List<Source> methodSources) {
            Object[] actuals;
            List<Argument> arguments;

            actuals = null; // TODO
            arguments = new ArrayList<>();
            return new MethodContextFactory(context, method, arguments, actuals);
        }
    }

    public static class MethodContextFactory extends ContextFactory {
        public static ContextFactory create(Context context, Repository schema, Method method, List<Source> initialSources) {
            List<Argument> arguments;
            List<Context> remainingContext;
            List<Source> remainingSources;
            Parameter[] formals;
            Object[] actuals;
            Parameter formal;
            Object ctx;
            Source source;

            arguments = new ArrayList<>();
            remainingContext = context.parentList();
            remainingSources = new ArrayList<>(initialSources);
            formals = method.getParameters();
            actuals = new Object[formals.length];
            for (int i = 0; i < formals.length; i++) {
                formal = formals[i];
                ctx = Context.remove(remainingContext, formal.getType());
                if (ctx != null) {
                    actuals[i] = ctx;
                } else if (remainingSources.isEmpty()) {
                    return null; // too many constructor arguments
                } else {
                    source = remainingSources.remove(0);
                    arguments.add(new Argument(context, source, new TargetParameter(schema, formal.getParameterizedType(), actuals, i)));
                }
            }
            if (!remainingSources.isEmpty()) {
                return null; // not all arguments matched
            }
            return new MethodContextFactory(context, method, arguments, actuals);
        }

        private final Context context;
        private final Method method;
        private final Object[] actuals;

        public MethodContextFactory(Context context, Method method, List<Argument> arguments, Object[] actuals) {
            super(arguments);
            this.method = method;
            this.context = context;
            this.actuals = actuals;
        }

        @Override
        public Object newInstance(Map<Context, Object> instantiatedContexts) throws Throwable {
            Object instance;

            instance = instantiatedContexts.get(context);
            return method.invoke(instance, actuals);
        }
    }

    public static class ClassHandle extends Handle {
        private final Class<?> clazz;

        public ClassHandle(Class<?> clazz) {
            this.clazz = clazz;
        }

        public ExceptionHandler exceptionHandler() {
            return null;
        }

        public Class<?> clazz() {
            return clazz;
        }

        public ContextFactory compile(Context context, Repository schema, List<Source> constructorSources) {
            ContextFactory found;
            ContextFactory candidate;

            found = null;
            for (Constructor constructor : clazz.getDeclaredConstructors()) {
                candidate = ConstructorContextFactory.createOpt(context, schema, constructor, constructorSources);
                if (candidate != null) {
                    if (found != null) {
                        throw new InvalidCliException("constructor is ambiguous: " + clazz.getName());
                    }
                    found = candidate;
                }
            }
            if (found == null) {
                throw new InvalidCliException("no matching constructor: " + clazz.getName() + "(" + names(constructorSources) + ")");
            }
            return found;
        }

        private static String names(List<Source> sources) {
            StringBuilder result;

            result = new StringBuilder();
            for (Source source : sources) {
                if (result.length() > 0) {
                    result.append(", ");
                }
                result.append(source.getName());
            }
            return result.toString();
        }
    }

    //--

    public static class ConstructorContextFactory extends ContextFactory {
        public static ContextFactory createOpt(Context context, Repository schema, Constructor constructor, List<Source> initialSources) {
            List<Argument> arguments;
            List<Context> remainingContext;
            List<Source> remainingSources;
            Parameter[] formals;
            Object[] actuals;
            Parameter formal;
            Object ctx;
            Source source;

            arguments = new ArrayList<>();
            remainingContext = context.parentList();
            remainingSources = new ArrayList<>(initialSources);
            formals = constructor.getParameters();
            actuals = new Object[formals.length];
            for (int i = 0; i < formals.length; i++) {
                formal = formals[i];
                ctx = Context.remove(remainingContext, formal.getType());
                if (ctx != null) {
                    actuals[i] = ctx;
                } else if (remainingSources.isEmpty()) {
                    return null; // too many constructor arguments
                } else {
                    source = remainingSources.remove(0);
                    arguments.add(new Argument(context, source, new TargetParameter(schema, formal.getParameterizedType(), actuals, i)));
                }
            }
            if (!remainingSources.isEmpty()) {
                return null; // not all arguments matched
            }
            return new ConstructorContextFactory(constructor, actuals, arguments);
        }

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
}
