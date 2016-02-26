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
package net.oneandone.inline.commands;

import net.oneandone.inline.Console;

import java.util.Locale;

public class PackageVersion {
    private final Console console;

    public PackageVersion(Console console) {
        this.console = console;
    }

    public void run() {
        Package pkg;

        pkg = getClass().getPackage();
        if (pkg == null) {
            console.info.println("unknown version");
        } else if (console.getVerbose()) {
            console.verbose.println(pkg.getName());
            console.verbose.println("  specification title: " + pkg.getSpecificationTitle());
            console.verbose.println("  specification version: " + pkg.getSpecificationVersion());
            console.verbose.println("  specification vendor: " + pkg.getSpecificationVendor());
            console.verbose.println("  implementation title: " + pkg.getImplementationTitle());
            console.verbose.println("  implementation version: " + pkg.getImplementationVersion());
            console.verbose.println("  implementation vendor: " + pkg.getImplementationVendor());
            console.verbose.println();
            console.verbose.println("Java Version: " + System.getProperty("java.version"));
            console.verbose.println("Platform encoding: " + System.getProperty("file.encoding"));
            console.verbose.println("Default Locale: " + Locale.getDefault());
            console.verbose.println("Scanner Locale: " + console.input.locale());
        } else {
            console.info.println(pkg.getSpecificationVersion());
        }
    }
}
