package net.oneandone.inline.parser;

import java.util.List;
import java.util.Map;

public abstract class ContextFactory {
    private final List<Argument> arguments;

    public ContextFactory(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public List<Argument> arguments() {
        return arguments;
    }

    public abstract Object newInstance(Map<Context, Object> instantiatedContexts) throws Throwable;
}
