/*
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

import java.io.IOException;
import java.util.List;

/**
 * Cli example with a single command. Demonstrates a cli with a single command, i.e. there is no first argument to
 * choose between different commands. This is done with Cli.single() (Note that is not the typical use case, you
 * normally have multiple commands and use Cli.create())
*/
public class Single {
    public static void main(String[] args) throws IOException {
        Cli cli;

        cli = Cli.single(Single.class, "ignored -flag=true -number first remaining*");
        System.exit(cli.run("first", "-number", "42"));
    }

    private boolean flag;

    private int number;

    private String first;

    private List<String> remaining;

    public Single(boolean flag, int number, String first, List<String> remaining) {
        this.flag = flag;
        this.number = number;
        this.first = first;
        this.remaining = remaining;
    }

    public void addRemaining(String str) {
        remaining.add(str);
    }

    public void run() {
        System.out.println("command invoked with ");
        System.out.println("   flag = " + flag);
        System.out.println("   number = " + number);
        System.out.println("   first = " + first);
        System.out.println("   remaining = " + remaining);
    }
}
