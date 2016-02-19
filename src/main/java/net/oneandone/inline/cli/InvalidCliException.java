package net.oneandone.inline.cli;

/** Thrown when building the Cli. */
public class InvalidCliException extends RuntimeException {
    public InvalidCliException(String message) {
        super(message);
    }
}
