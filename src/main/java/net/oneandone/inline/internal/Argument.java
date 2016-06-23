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
package net.oneandone.inline.internal;

import net.oneandone.inline.ArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Associates a source with a target. */
public class Argument {
    public final Context context;
    public final Source source;
    public final Target target; // type of the argument/field where to store

    public Argument(Context context, Source source, Target target) {
        this.context = context;
        this.source = source;
        this.target = target;
    }

    public void set(Object dest, List<String> actual, Map<String, String> defaults) {
        String d;
        List<Object> lst;
        Object value;
        String name;
        String dv;
        int idx;

        if (target.isList()) {
            lst = new ArrayList<>();
            for (String str : actual) {
                 lst.add(parse(str));
            }
            value = lst;
        } else {
            if (actual.isEmpty()) {
                d = source.getDefaultString();
                if (d.startsWith("@")) {
                    if (d.length() == 1) {
                        name = source.getName();
                        dv = Source.DEFAULT_UNDEFINED;
                    } else {
                        name = d.substring(1);
                        idx = name.indexOf(':');
                        if (idx == -1) {
                            dv = Source.DEFAULT_UNDEFINED;
                        } else {
                            dv = name.substring(idx + 1);
                            name = name.substring(0, idx);
                        }
                    }
                    d = defaults.get(name);
                    if (d == null) {
                        d = dv;
                    }
                }
                value = dflt(d);
            } else {
                value = parse(actual.get(0));
            }
        }
        target.doSet(dest, value);
    }

    private Object dflt(String str) {
        if (Source.DEFAULT_UNDEFINED.equals(str)) {
            return target.defaultComponent();
        } else if ("null".equals(str)) {
            return null;
        } else try {
            return  parse(str);
        } catch (ArgumentException e) {
            throw new IllegalStateException("cannot convert default value to type " + target + ": " + str);
        }
    }

    private Object parse(String str) {
        try {
            return target.stringToComponent(str);
        } catch (RuntimeException e) {
            throw new ArgumentException("invalid argument " + source.getName() + ": expected " + target.expected() + ", got '" + str + '"', e);
        }
    }
}
