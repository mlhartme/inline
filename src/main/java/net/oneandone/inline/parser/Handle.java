package net.oneandone.inline.parser;

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
}
