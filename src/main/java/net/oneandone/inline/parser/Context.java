package net.oneandone.inline.parser;

import net.oneandone.inline.types.Repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines how to create an context object. Context objects are command objects or objects used for command objects.
 * "compiles" into a ContextBuilder.
 */
public class Context {
    public static Context create(Context parent, String explicitName, Object classOrInstance, String definition) {
        String name;
        int idx;
        String syntax;
        String mapping;

        idx = definition.indexOf('{');
        if (idx == -1) {
            syntax = definition;
            mapping = "";
        } else {
            if (!definition.endsWith("}")) {
                throw new InvalidCliException(definition);
            }
            mapping = definition.substring(idx + 1, definition.length() - 1).trim();
            syntax = definition.substring(0, idx).trim();
        }
        Handle handle = new Handle(classOrInstance);
        if (explicitName == null) {
            name = handle.clazz().getName();
        } else {
            name = explicitName;
        }
        return new Context(parent, name, handle, Source.forSyntax(syntax), Mapping.parse(mapping, handle.clazz()));
    }

    /** may be null */
    public final Context parent;
    public final String name;
    public final Handle handle;
    public final List<Source> sources;
    public final Mapping mapping;

    private ContextBuilder lazyCompiledContext;

    public Context(Context parent, String name, Handle handle, List<Source> sources, Mapping mapping) {
        this.parent = parent;
        this.name = name;
        this.handle = handle;
        this.sources = sources;
        this.mapping = mapping;
        this.lazyCompiledContext = null;
    }

    public ContextBuilder compile(Repository schema) {
        if (lazyCompiledContext == null) {
            lazyCompiledContext = doCompile(schema);
        }
        return lazyCompiledContext;
    }

    private ContextBuilder doCompile(Repository schema) {
        Class<?> clazz;
        List<Source> constructorSources;
        List<Source> extraSources;
        Object[] actuals;
        List<Argument> arguments;
        Constructor found;
        Object[] foundActuals;
        List<Argument> foundArguments;
        ContextBuilder result;

        found = null;
        foundActuals = null;
        foundArguments = null;
        arguments = new ArrayList<>();
        constructorSources = new ArrayList<>(sources.size());
        extraSources = new ArrayList<>();
        for (Source s : sources) {
            if (mapping.contains(s.getName())) {
                extraSources.add(s);
            } else {
                constructorSources.add(s);
            }
        }
        if (handle.isClass()) {
            clazz = handle.clazz();
            for (Constructor constructor : clazz.getDeclaredConstructors()) {
                arguments.clear();
                actuals = match(schema, constructor, constructorSources, arguments);
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
            result = new ContextBuilder(this, compiledParent(schema), found, foundActuals);
            for (Argument a : foundArguments) {
                result.addArgument(a);
            }
        } else {
            if (!constructorSources.isEmpty()) {
                throw new InvalidCliException("cannot apply constructor argument to an instance");
            }
            result = new ContextBuilder(this, compiledParent(schema), handle.instance());
        }
        for (Source s : extraSources) {
            result.addArgument(new Argument(this, s, mapping.target(schema, s.getName())));
        }
        return result;
    }

    private ContextBuilder compiledParent(Repository schema) {
        return parent == null ? null : parent.compile(schema);
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

    private Object[] match(Repository schema, Constructor constructor, List<Source> initialSources, List<Argument> result) {
        List<Context> remainingContext;
        List<Source> remainingSources;
        Parameter[] formals;
        Object[] actuals;
        Parameter formal;
        Object ctx;
        Source source;

        remainingContext = parentList();
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
                result.add(new Argument(this, source, new TargetParameter(schema, formal.getParameterizedType(), actuals, i)));
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

    public List<Context> parentList() {
        List<Context> result;

        result = new ArrayList<>();
        for (Context context = parent; context != null; context = context.parent) {
            result.add(context);
        }
        return result;
    }
}
