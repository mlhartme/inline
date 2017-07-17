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

import net.oneandone.inline.Base;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines how to create an context object. Context objects are command objects or objects used for command objects.
 * "compiles" into a ContextBuilder.
 */
public class Context {
    public static Context create(Context parent, Base base, String explicitName, Handle handle, String definition) {
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
            name = handle.name();
        } else {
            name = explicitName;
        }
        return new Context(parent, name, handle, Source.forSyntax(base.fullSyntax(syntax)), Mapping.parse(base.fullMapping(mapping), handle.clazz()));
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

    public ContextBuilder compile(Repository repository) {
        if (lazyCompiledContext == null) {
            lazyCompiledContext = doCompile(repository);
        }
        return lazyCompiledContext;
    }

    private ContextBuilder doCompile(Repository repository) {
        List<Source> constructorSources;
        List<Source> extraSources;
        ContextBuilder result;
        ContextFactory factory;

        constructorSources = new ArrayList<>(sources.size());
        extraSources = new ArrayList<>();
        for (Source s : sources) {
            if (mapping.contains(s.getName())) {
                extraSources.add(s);
            } else {
                constructorSources.add(s);
            }
        }
        factory = handle.compile(this, repository, constructorSources);
        result = new ContextBuilder(this, compiledParent(repository), factory);
        for (Argument a : factory.arguments()) {
            result.addArgument(a);
        }
        for (Source s : extraSources) {
            result.addArgument(new Argument(this, s, mapping.target(repository, s.getName())));
        }
        return result;
    }

    private ContextBuilder compiledParent(Repository repository) {
        return parent == null ? null : parent.compile(repository);
    }

    public List<Context> parentList() {
        List<Context> result;

        result = new ArrayList<>();
        for (Context context = parent; context != null; context = context.parent) {
            result.add(context);
        }
        return result;
    }

    //--

    public static Context remove(List<Context> parents, Class<?> type) {
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
}
