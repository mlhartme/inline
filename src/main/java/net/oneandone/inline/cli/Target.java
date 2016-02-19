package net.oneandone.inline.cli;

import net.oneandone.inline.types.Repository;
import net.oneandone.inline.types.Type;
import net.oneandone.inline.types.ParseException;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/** Defines where to store values from the command line. */
public abstract class Target {
    private final boolean list;
    private final Type component;

    public Target(Repository schema, java.lang.reflect.Type type) {
        ParameterizedType p;
        java.lang.reflect.Type[] args;

        if (type instanceof Class) {
            this.list = false;
            this.component = schema.get((Class) type);
        } else if (type instanceof ParameterizedType) {
            p = (ParameterizedType) type;
            args = p.getActualTypeArguments();
            if (!p.getRawType().equals(List.class)) {
                throw new IllegalArgumentException("not a list: " + type.toString());
            }
            if (args.length != 1) {
                throw new IllegalArgumentException("too many type parameter: " + type.toString());
            }
            if (!(args[0] instanceof Class)) {
                throw new IllegalArgumentException("too much nesting: " + type.toString());
            }
            this.list = true;
            this.component = schema.get((Class) args[0]);
        } else {
            throw new IllegalArgumentException("unsupported type: " + type);
        }
    }

    public Target(boolean list, Type component) {
        this.list = list;
        this.component = component;
    }

    /** return true to set pass values before command object instantiation */
    public abstract boolean before();

    /** value is an instance of the reflection type */
    public abstract void doSet(Object dest, Object value);

    public boolean isList() {
        return list;
    }

    public boolean isBoolean() {
        return !list && (component.getRawType().equals(Boolean.TYPE) || component.getRawType().equals(Boolean.class));
    }

    public Object stringToComponent(String str) throws ParseException {
        return component.parse(str);
    }

    public Object newComponent() {
        return component.newInstance();
    }
}
