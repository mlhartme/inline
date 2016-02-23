package net.oneandone.inline.parser;

import net.oneandone.inline.types.Repository;
import net.oneandone.inline.types.Primitive;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/** Defines where to store values from the command line. */
public abstract class Target {
    private final boolean list;
    private final Primitive component;

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
                throw new InvalidCliException("not a list: " + type.toString());
            }
            if (args.length != 1) {
                throw new InvalidCliException("too many type parameter: " + type.toString());
            }
            if (!(args[0] instanceof Class)) {
                throw new InvalidCliException("too much nesting: " + type.toString());
            }
            this.list = true;
            this.component = schema.get((Class) args[0]);
        } else {
            throw new InvalidCliException("unsupported type: " + type);
        }
    }

    public Target(boolean list, Primitive component) {
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

    public Object stringToComponent(String str) {
        return component.parse(str);
    }

    public Object defaultComponent() {
        return component.defaultValue();
    }

    public String expected() {
        return component.expected;
    }
}
