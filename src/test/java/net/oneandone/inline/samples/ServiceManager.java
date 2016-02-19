package net.oneandone.inline.samples;

import net.oneandone.inline.Cli;
import net.oneandone.inline.Console;

/**
 * Dummy implementation of the 'service' command. Demonstraces contexts.
 */
public class ServiceManager {
    public static void main(String[] args) {
        Cli cli;

        cli = Cli.create("usage:\n"
                + "list            list available services\n"
                + "<name> start    starts the specified service\n"
                + "<name> stop     stop the specified service\n");

        cli.add(Ls.class, "list");
        cli.begin(Service.class, "service" );
          cli.add(Start.class, "start");
          cli.add(Stop.class, "stop");

        cli.run("list");
        cli.run("apache", "start");
        cli.run("apache", "stop");
    }

    public static final String[] AVAILABLE = { "apache", "tomcat"};

    public static class Service {
        private final String name;

        public Service(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public static class Base {
        protected final Console console;

        public Base(Console console) {
            this.console = console;
        }
    }

    public static class Ls extends Base {
        public Ls(Console console) {
            super(console);
        }

        public void run() {
            for (String name : AVAILABLE) {
                console.info.println(name);
            }
        }
    }

    public static class Start extends Base {
        private final Service service;

        public Start(Console console, Service service) {
            super(console);
            this.service = service;
        }

        public void run() {
            console.info.println("start " + service);
        }
    }

    public static class Stop extends Base {
        protected final Service service;

        public Stop(Console console, Service service) {
            super(console);
            this.service = service;
        }

        public void run() {
            console.info.println("stop " + service);
        }
    }
}
