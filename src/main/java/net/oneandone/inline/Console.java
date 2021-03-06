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
package net.oneandone.inline;

import net.oneandone.inline.util.SwitchableWriter;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Configurable replacement for System.out, System.err and System.in. 
 */
public class Console {
    public static Console create() {
        return new Console(new PrintWriter(System.out, true), new PrintWriter(System.err, true), System.in);
    }

    public final PrintWriter info;
    public final PrintWriter verbose;
    public final PrintWriter error;
    public final Scanner input;
    private boolean stacktraces;
    
    private final SwitchableWriter verboseSwitch;
    
    public Console(PrintWriter info, PrintWriter error, InputStream in) {
        this.info = info;
        this.verboseSwitch = new SwitchableWriter(info, false);
        this.verbose = new PrintWriter(verboseSwitch, true);
        this.error = error;
        this.input = new Scanner(in);
        this.stacktraces = false;
    }

    public boolean getStacktraces() {
        return stacktraces;
    }

    public void setStacktraces(boolean s) {
        stacktraces = s;
    }

    public boolean getVerbose() {
        return verboseSwitch.getEnabled();
    }

    public void setVerbose(boolean verbose) {
        verboseSwitch.setEnabled(verbose);
    }
    
    public void pressReturn() {
        readline("Press return to continue, ctrl-C to abort.\n");
    }

    public String readline(String message) {
        return readline(message, "");
    }

    public String readline(String message, String dflt) {
        String str;
        
        info.print(message);
        info.flush();
        str = input.nextLine();
        if (str.length() == 0) {
            return dflt;
        } else {
            return str;
        }
    }

    //--

    public int handleException(Throwable throwable) {
        if (throwable instanceof ArgumentException) {
            error.println(throwable.getMessage());
            info.println("Specify 'help' to get a usage message.");
            throwable.printStackTrace(stacktraces ? error : verbose);
            return -1;
        }
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        error.println(throwable.getMessage());
        throwable.printStackTrace(stacktraces ? error : verbose);
        return -1;
    }
}
