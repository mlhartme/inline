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

import java.io.IOException;
import java.util.List;

/**
 * Cli example with two commands.
*/
public class BaseClass {
    public static void main(String[] args) throws IOException {
        Cli cli;

        cli = Cli.create("demo help")
                .base(BaseCommand.class, "-batch { setBatch(batch) }")
                  .add(MyCommand.class,  "my -flag=false -number=7 first remaining*");
        System.exit(cli.run("my", "-batch", "one", "two", "-v", "three"));
    }

    public static class BaseCommand {
        protected boolean batch = false;

        public void setBatch(boolean batch) {
            this.batch = batch;
        }
    }

    public static class MyCommand extends BaseCommand {
        private final Console console;

        private final String one;
        private final boolean flag;
        private final int number;
        private final List<String> remaining;

        public MyCommand(Console console, boolean flag, int number, String one, List<String> remaining) {
            this.console = console;
            this.one = one;
            this.flag = flag;
            this.number = number;
            this.remaining = remaining;
        }

        public void run() {
            console.verbose.println("verbose output");
            console.info.println("invoked 'first' with ");
            console.info.println("   batch = " + batch);
            console.info.println("   flag = " + flag);
            console.info.println("   number = " + number);
            console.info.println("   one = " + one);
            console.info.println("   remaining = " + remaining);
        }
    }
}
