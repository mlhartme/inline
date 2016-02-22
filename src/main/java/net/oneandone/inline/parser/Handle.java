package net.oneandone.inline.parser;

import net.oneandone.inline.types.Repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reference to a Class or an Instance. A ContextFactory factory. */
public abstract class Handle {
    public static Handle create(Object classOrInstance) {
        if (classOrInstance instanceof Class<?>) {
            return new ClassHandle((Class) classOrInstance);
        } else {
            return new InstanceHandle(classOrInstance);
        }
    }

    public abstract Class<?> clazz();
    public abstract boolean isClass();
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

        public boolean isClass() {
            return false;
        }

        public ContextFactory compile(Context context, Repository schema, List<Source> constructorSources) {
            if (!constructorSources.isEmpty()) {
                throw new InvalidCliException("cannot apply constructor argument to an instance");
            }
            return new IdentityContextFactory(instance);
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

        public boolean isClass() {
            return true;
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
                ctx = eatContext(remainingContext, formal.getType());
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

        private static Context eatContext(List<Context> parents, Class<?> type) {
            Context context;

            for (int i = 0, max = parents.size(); i < max; i++) {
                context = parents.get(i);
                if (type.isAssignableFrom(context.handle.clazz())) {
                    parents.remove(i);
                    return context;
                }
            }
            return null;
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
