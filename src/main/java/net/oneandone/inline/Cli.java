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
import net.oneandone.inline.parser.ArgumentException;
import net.oneandone.inline.parser.Command;
import net.oneandone.inline.parser.Context;
import net.oneandone.inline.parser.ContextBuilder;
import net.oneandone.inline.parser.ExceptionHandler;
import net.oneandone.inline.parser.Handle;
import net.oneandone.inline.parser.InvalidCliException;
import net.oneandone.inline.parser.Mapping;
import net.oneandone.inline.types.Repository;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A command line interface. Define available command with begin(), add() and end(). Then invoke run() to actually invoke them.
 */
public class Cli {
    public static Cli single(Class<?> command, String syntax) throws IOException {
        Console console;
        Cli cli;

        console = Console.create();
        cli = new Cli().begin(console, "-v -e  { setVerbose(v) setStacktraces(e) }");
        cli.add(command, syntax);
        return cli;
    }

    public static Cli create(String help) {
        Console console;
        Cli cli;

        console = Console.create();
        cli = new Cli()
                .begin(console, "-v -e  { setVerbose(v) setStacktraces(e) }")
                   .addDefault(new Help(console, help), "help")
                   .add(PackageVersion.class, "version");
        return cli;
    }

    protected final Repository schema;
    private final List<Command> commands;
    private Command defaultCommand;
    private Context currentContext;
    private ExceptionHandler exceptionHandler;

    public Cli() {
        this(new Repository());
    }
    
    public Cli(Repository schema) {
        this.schema = schema;
        this.commands = new ArrayList<>();
        this.currentContext = null;
        this.defaultCommand = null;
    }

    public Cli begin(Object context) {
        return begin(context, "");
    }

    public Cli begin(Object context, String syntax) {
        return begin(null, context, syntax);
    }

    public Cli begin(String name, Object context, String syntax) {
        ExceptionHandler h;
        Handle handle;

        if (context == null) {
            throw new IllegalArgumentException();
        }
        handle = new Handle(context);
        h = handle.exceptionHandler();
        if (h != null) {
            if (exceptionHandler != null) {
                throw new InvalidCliException("duplicate exception handler: " + exceptionHandler + " vs "+ h);
            }
            exceptionHandler = h;
        }
        this.currentContext = Context.create(currentContext, name, handle, syntax);
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
        context = Context.create(currentContext, null, new Handle(clazzOrInstance), definition);
        builder = context.compile(schema);
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
            return clazz.getDeclaredMethod(name, NO_ARGS);
        } catch (NoSuchMethodException e) {
            throw new InvalidCliException("command method not found: " + clazz.getName() + "." + name + "()");
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
                obj = c.getBuilder().run(args);
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
                obj = c.getBuilder().run(lst);
            }
            return c.run(obj);
        } catch (Throwable e) {
            return exceptionHandler.handleException(e);
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
