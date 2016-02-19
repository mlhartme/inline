package net.oneandone.inline.cli;

@FunctionalInterface
public interface ExceptionHandler {
    int handleException(Throwable throwable);
}
