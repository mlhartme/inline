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
package net.oneandone.inline.util;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class SwitchableWriter extends Writer {
    private final Writer dest;
    private boolean enabled;

    public SwitchableWriter(Writer dest, boolean enabled) {
        this.dest = dest;
        this.enabled = enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getEnabled() {
        return enabled;
    }

    //--
    
    @Override
    public void write(int c) throws IOException {
        if (enabled) {
            dest.write(c);
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (enabled) {
            dest.write(cbuf, off, len);
        }
    }

    @Override
    public void flush() throws IOException {
        if (enabled) {
            dest.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (enabled) {
            dest.close();
        }
    }
}
