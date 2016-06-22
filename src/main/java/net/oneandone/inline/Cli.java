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
package net.oneandone.inline;

import net.oneandone.inline.commands.Help;
import net.oneandone.inline.commands.PackageVersion;
import net.oneandone.inline.internal.Command;
import net.oneandone.inline.internal.Context;
import net.oneandone.inline.internal.ContextBuilder;
import net.oneandone.inline.internal.Handle;
import net.oneandone.inline.internal.InvalidCliException;
import net.oneandone.inline.internal.Mapping;
import net.oneandone.inline.internal.Repository;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A command line interface. Define available command with begin(), add() and end(). Then invoke run() to actually invoke them.
 */
public class Cli {
    public static Cli create(String help) {
        Console console;
        Cli cli;

        console = Console.create();
        cli = new Cli(new Repository(), console::handleException);
        cli.begin(console, "-v -e  { setVerbose(v) setStacktraces(e) }")
              .addDefault(new Help(console, help), "help")
              .add(PackageVersion.class, "version");
        return cli;
    }

    public static Cli single(Class<?> command, String syntax) throws IOException {
        Console console;
        Cli cli;

        console = Console.create();
        cli = new Cli(new Repository(), console::handleException);
        cli.begin(console, "-v -e  { setVerbose(v) setStacktraces(e) }");
        cli.add(command, syntax);
        return cli;
    }

    protected final Repository repository;
    private final Function<Throwable, Integer> exceptionHandler;
    private final List<Command> commands;
    private Context currentContext;
    private Base currentBase;
    private Command defaultCommand;
    private final Map<String, String> defaults;

    public Cli() {
        this(e -> { e.printStackTrace(); return -1; });
    }

    public Cli(Function<Throwable, Integer> exceptionHandler) {
        this(new Repository(), exceptionHandler);
    }

    public Cli(Repository repository, Function<Throwable, Integer> exceptionHandler) {
        this.repository = repository;
        this.commands = new ArrayList<>();
        this.currentContext = null;
        this.currentBase = new Base(null, "", "");
        this.defaultCommand = null;
        this.exceptionHandler = exceptionHandler;
        this.defaults = new HashMap<>();
    }

    public Cli primitive(Class<?> clazz, String expected, Object dflt, Function<String, Object> f) {
        repository.register(clazz, expected, dflt, f);
        return this;
    }

    public Cli base(Class<?> base, String syntax) {
        currentBase = Base.create(currentBase, base, syntax);
        return this;
    }

    public void defaults(Map<String, String> map) {
        defaults.putAll(map);
    }

    public Cli begin(Object context) {
        return begin(context, "");
    }

    public Cli begin(Object context, String definition) {
        return begin(null, context, definition);
    }

    public Cli begin(String name, Object context, String definition) {
        Handle handle;

        if (context == null) {
            throw new IllegalArgumentException();
        }
        handle = Handle.create(currentContext, context);
        this.currentContext = Context.create(currentContext, currentBase, name, handle, definition);
        return this;
    }

    public Cli end() {
        if (currentContext == null) {
            throw new InvalidCliException("end without context");
        }
        currentContext = currentContext.parent;
        return this;
    }

    public Cli add(Object classOrInstance, String definition) {
        return doAdd(classOrInstance, definition, false);
    }

    public Cli addDefault(Object classOrInstance, String definition) {
        return doAdd(classOrInstance, definition, true);
    }

    private Cli doAdd(Object clazzOrInstance, String definition, boolean dflt) {
        Context context;
        int idx;
        String name;
        Command command;
        ContextBuilder builder;

        idx = definition.indexOf(' ');
        if (idx == -1) {
            name = definition;
            definition = "";
        } else {
            name = definition.substring(0, idx);
            definition = definition.substring(idx + 1);
        }
        context = Context.create(currentContext, currentBase, null, Handle.create(currentContext, clazzOrInstance), definition);
        builder = context.compile(repository);
        if (lookup(name) != null) {
            throw new IllegalArgumentException("duplicate command: " + name);
        }
        command = new Command(builder, name, commandMethod(clazzOrInstance, context.mapping));
        commands.add(command);
        if (dflt) {
            defaultCommand = command;
        }
        return this;
    }

    private static final Class<?>[] NO_ARGS = {};

    private static Method commandMethod(Object classOrInstance, Mapping mapping) {
        Class<?> clazz;
        String name;

        name = mapping.getCommand();
        if (name == null) {
            name = "run";
        }
        if (classOrInstance instanceof Class<?>) {
            clazz = (Class<?>) classOrInstance;
        } else {
            clazz = classOrInstance.getClass();
        }
        try {
            return clazz.getMethod(name, NO_ARGS);
        } catch (NoSuchMethodException e) {
            throw new InvalidCliException("command method not found: public " + clazz.getName() + "." + name + "()");
        }
    }

    public int run(String... args) {
        return run(Arrays.asList(args));
    }

    public int run(List<String> args) {
        Object obj;
        Command c;
        String name;
        List<String> lst;

        if (exceptionHandler == null) {
            throw new InvalidCliException("missing exception handler");
        }
        try {
            if (commands.size() == 1) {
                c = commands.get(0);
                obj = c.getBuilder().run(defaults, args);
            } else {
                lst = new ArrayList<>(args);
                name = eatCommand(lst);
                if (name == null) {
                    c = defaultCommand;
                    if (c == null) {
                        throw new ArgumentException("missing command");
                    }
                } else {
                    c = get(name);
                }
                obj = c.getBuilder().run(defaults, lst);
            }
            return c.run(obj);
        } catch (Throwable e) {
            return exceptionHandler.apply(e);
        }
    }

    private String eatCommand(List<String> args) {
        String arg;

        for (int i = 0, max = args.size(); i < max; i++) {
            arg = args.get(i);
            if (!ContextBuilder.isOption(arg)) {
                args.remove(i);
                return arg;
            }
        }
        return null;
    }

    public Command get(String name) {
        Command result;

        result = lookup(name);
        if (result == null) {
            throw new ArgumentException("command not found: " + name);
        }
        return result;
    }

    public Command lookup(String name) {
        for (Command method : commands) {
            if (name.equals(method.getName())) {
                return method;
            }
        }
        return null;
    }
}
