package net.oneandone.inline.parser;

import net.oneandone.inline.types.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines how to create an context object. Context objects are command objects or objects used for command objects.
 * "compiles" into a ContextBuilder.
 */
public class Context {
    public static Context create(Context parent, String explicitName, Handle handle, String definition) {
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
        List<Source> constructorSources;
        List<Source> extraSources;
        ContextBuilder result;
        ContextBuilder.ContextFactory factory;

        constructorSources = new ArrayList<>(sources.size());
        extraSources = new ArrayList<>();
        for (Source s : sources) {
            if (mapping.contains(s.getName())) {
                extraSources.add(s);
            } else {
                constructorSources.add(s);
            }
        }
        factory = handle.compile(this, schema, constructorSources);
        result = new ContextBuilder(this, compiledParent(schema), factory);
        for (Argument a : factory.arguments()) {
            result.addArgument(a);
        }
        for (Source s : extraSources) {
            result.addArgument(new Argument(this, s, mapping.target(schema, s.getName())));
        }
        return result;
    }

    private ContextBuilder compiledParent(Repository schema) {
        return parent == null ? null : parent.compile(schema);
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
