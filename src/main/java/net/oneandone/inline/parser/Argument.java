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
package net.oneandone.inline.parser;

import java.util.ArrayList;
import java.util.List;

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

    public void set(Object dest, List<String> actual) {
        String d;
        List<Object> lst;
        Object value;

        if (target.isList()) {
            lst = new ArrayList<>();
            for (String str : actual) {
                 lst.add(parse(str));
            }
            value = lst;
        } else {
            if (actual.isEmpty()) {
                d = source.getDefaultString();
                if (Source.DEFAULT_UNDEFINED.equals(d)) {
                    value = target.defaultComponent();
                } else {
                    try {
                        value = parse(d);
                    } catch (ArgumentException e) {
                        throw new IllegalStateException("cannot convert default value to type " + target + ": " + d);
                    }
                }
            } else {
                value = parse(actual.get(0));
            }
            target.doSet(dest, value);
        }
        target.doSet(dest, value);
    }

    private Object parse(String str) {
        try {
            return target.stringToComponent(str);
        } catch (RuntimeException e) {
            throw new ArgumentException("invalid argument " + source.getName() + ": expected " + target.expected() + ", got '" + str + '"', e);
        }
    }
}
