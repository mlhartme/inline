package net.oneandone.inline.parser;

import net.oneandone.inline.types.Repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class Handle {
    private final Object classOrInstance;

    public Handle(Object classOrInstance) {
        this.classOrInstance = classOrInstance;
    }

    public boolean isClass() {
        return classOrInstance instanceof Class<?>;
    }

    public Class<?> clazz() {
        if (isClass()) {
            return (Class) classOrInstance;
        } else {
            return classOrInstance.getClass();
        }
    }

    public Object instance() {
        if (isClass()) {
            throw new IllegalStateException();
        }
        return classOrInstance;
    }

    public ContextBuilder.ContextFactory compile(Context context, Repository schema, List<Source> constructorSources) {
        Class<?> clazz;
        Object[] actuals;
        List<Argument> arguments;
        Constructor found;
        Object[] foundActuals;
        List<Argument> foundArguments;

        found = null;
        foundActuals = null;
        foundArguments = null;
        arguments = new ArrayList<>();
        if (isClass()) {
            clazz = clazz();
            for (Constructor constructor : clazz.getDeclaredConstructors()) {
                arguments.clear();
                actuals = match(context, schema, constructor, constructorSources, arguments);
                if (actuals != null) {
                    if (found != null) {
                        throw new InvalidCliException("constructor is ambiguous: " + clazz.getName());
                    }
                    found = constructor;
                    foundActuals = actuals;
                    foundArguments = new ArrayList<>(arguments);
                }
            }
            if (found == null) {
                throw new InvalidCliException("no matching constructor: " + clazz.getName() + "(" + names(constructorSources) + ")");
            }
            return new ContextBuilder.ConstructorContextFactory(found, foundActuals, foundArguments);
        } else {
            if (!constructorSources.isEmpty()) {
                throw new InvalidCliException("cannot apply constructor argument to an instance");
            }
            return new ContextBuilder.IdentityContextFactory(instance());
        }
    }

    private Object[] match(Context context, Repository schema, Constructor constructor, List<Source> initialSources, List<Argument> result) {
        List<Context> remainingContext;
        List<Source> remainingSources;
        Parameter[] formals;
        Object[] actuals;
        Parameter formal;
        Object ctx;
        Source source;

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
                result.add(new Argument(context, source, new TargetParameter(schema, formal.getParameterizedType(), actuals, i)));
            }
        }
        if (!remainingSources.isEmpty()) {
            return null; // not all arguments matched
        }
        return actuals;
    }

    private static Object eatContext(List<Context> parents, Class<?> type) {
        Context context;
        boolean isClass;

        for (int i = 0, max = parents.size(); i < max; i++) {
            context = parents.get(i);
            isClass = context.handle.isClass();
            if (type.isAssignableFrom(context.handle.clazz())) {
                parents.remove(i);
                return isClass ? context : context.handle.instance();
            }
        }
        return null;
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
