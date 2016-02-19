package net.oneandone.inline.parser;

@FunctionalInterface
public interface ExceptionHandler {
    int handleException(Throwable throwable);
}
