package net.oneandone.inline.internal;

import net.oneandone.inline.util.Split;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Mapping {
    public static Mapping parse(String str, Class<?> clazz) {
        Mapping result;
        int idx;

        result = new Mapping();
        for (String item : Split.split(str)) {
            idx = item.indexOf('=');
            if (idx != -1) {
                result.addField(item.substring(idx + 1), clazz, item.substring(0, idx));
            } else {
                idx = item.indexOf('(');
                if (idx != -1) {
                    if (!item.endsWith(")")) {
                        throw new InvalidCliException("invalid method mapping: " + item);
                    }
                    result.addMethod(item.substring(idx + 1, item.length() - 1), clazz, item.substring(0, idx));
                } else {
                    result.command(item);
                }
            }
        }
        return result;
    }

    private String command;

    /** maps argument names to field names */
    private final Map<String, Field> fields;

    /** maps argument names to method names */
    private final Map<String, Method> methods;

    private final Map<String, Method> iteratedMethods;

    public Mapping() {
        this.command = null;
        this.fields = new HashMap<>();
        this.methods = new HashMap<>();
        this.iteratedMethods = new HashMap<>();
    }

    public String getCommand() {
        return command;
    }

    public void command(String cmd) {
        if (command != null) {
            throw new InvalidCliException("duplicate command mapping");
        }
        command = cmd;
    }

    public void addField(String argument, Class<?> clazz, String name) {
        Field field;

        try {
            field = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new InvalidCliException("no such field: " + clazz.getName() + "." + name);
        }
        if (fields.put(argument, field) != null) {
            throw new InvalidCliException("duplicate field mapping for argument " + argument);
        }
    }

    public void addMethod(String argument, Class<?> clazz, String name) {
        Method method;
        boolean iterable;

        method = null;
        iterable = name.endsWith("*");
        if (iterable) {
            name = name.substring(0, name.length() - 1);
        }
        for (Method candidate : clazz.getMethods()) {
            if (candidate.getParameterCount() == 1 && candidate.getName().equals(name)) {
                if (method != null) {
                    throw new InvalidCliException("method mapping ambiguous: " + name);
                }
                method = candidate;
            }
        }
        if (method == null) {
            throw new InvalidCliException("method not found: " + clazz.getName() + "." + name + "(x)");
        }
        if ((iterable ? iteratedMethods : methods).put(argument, method) != null) {
            throw new InvalidCliException("duplicate method mapping for argument " + argument);
        }
    }

    public boolean contains(String name) {
        return fields.containsKey(name) || methods.containsKey(name) || iteratedMethods.containsKey(name);
    }

    public Target target(Repository repository, String argument) {
        Field field;
        Method method;

        field = fields.get(argument);
        if (field != null) {
            return TargetField.create(repository, field);
        }
        method = methods.get(argument);
        if (method != null) {
            return TargetMethod.create(false, repository, method);
        }
        method = iteratedMethods.get(argument);
        if (method != null) {
            return TargetMethod.create(true, repository, method);
        }
        throw new IllegalStateException();
    }
}
