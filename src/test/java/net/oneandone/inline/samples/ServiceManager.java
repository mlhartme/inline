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
package net.oneandone.inline.samples;

import net.oneandone.inline.Cli;
import net.oneandone.inline.Console;
import net.oneandone.inline.ArgumentException;

import java.util.ArrayList;
import java.util.List;

/**
 * Dummy implementation of the 'service' command.
 */
public class ServiceManager {
    public static void main(String[] args) {
        Cli cli;

        //-- define cli
        cli = Cli.create("usage:\n"
                + "list            list available services\n"
                + "start <name>    starts the specified service\n"
                + "stop <name>     stop the specified service\n");
        cli.begin("manager", new ServiceManager(), "");
          cli.add(Ls.class, "list");
          cli.begin("manager.service", "service" );
            cli.add(Start.class, "start");
            cli.add(Stop.class, "stop");

        //-- use cli
        cli.run("list");
        cli.run("start", "apache");
        cli.run("stop", "apache");
    }

    public final List<Service> all;

    public ServiceManager() {
        all = new ArrayList<>();
        all.add(new Service("apache"));
        all.add(new Service("tomcat"));
    }

    public Service service(String name) {
        for (Service service : all) {
            if (name.equals(service.name)) {
                return service;
            }
        }
        throw new ArgumentException("unknown service: " + name);
    }

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
        private final ServiceManager manager;

        public Ls(ServiceManager manager, Console console) {
            super(console);
            this.manager = manager;
        }

        public void run() {
            for (Service service : manager.all) {
                console.info.println(service.name);
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
