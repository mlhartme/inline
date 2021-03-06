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
package net.oneandone.inline.internal;

import net.oneandone.inline.ArgumentException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Maps formals to actuals */
public class Actuals {
    private final Map<Argument, List<String>> actuals;
    private final Map<String, String> defaults;

    public Actuals(Map<String, String> defaults) {
        this.defaults = defaults;
        this.actuals = new HashMap<>();
    }

    public void defineAll(Collection<Argument> formals) {
        for (Argument formal : formals) {
            define(formal);
        }
    }

    public void define(Argument formal) {
        if (actuals.put(formal, new ArrayList<>()) != null) {
            throw new InvalidCliException("duplicate argument: " + formal);
        }
    }

    /** @return true if this formal argument has reached the max number of items. */
    public boolean add(Argument formal, String item) {
        List<String> value;

        value = actuals.get(formal);
        value.add(item);
        return value.size() == formal.source.max();
    }

    public void save(Context context, Object target) {
        Argument argument;

        for (Map.Entry<Argument, List<String>> entry : actuals.entrySet()) {
            argument = entry.getKey();
            if (argument.context == context) {
                if (argument.target.before() == (target == null)) {
                    argument.source.checkCardinality(entry.getValue().size());
                    argument.set(target, entry.getValue(), defaults);
                }
            }
        }
    }

    public void fill(List<String> args, Map<String, Argument> options, List<Argument> values) {
        boolean inOptions;
        int position;
        int assign;
        String arg;
        Argument argument;
        String value;
        StringBuilder builder;

        position = 0;
        inOptions = true;
        for (int i = 0, max = args.size(); i < max; i++) {
            arg = args.get(i);
            if (inOptions && ContextBuilder.isOption(arg)) {
                arg = arg.substring(1);
                assign = arg.indexOf('=');
                if (assign != -1) {
                    value = arg.substring(assign + 1);
                    arg = arg.substring(0, assign);
                } else {
                    value = null;
                }
                argument = options.get(arg);
                if (argument == null) {
                    throw new ArgumentException("unknown option " + arg);
                }
                if (value == null) {
                    if (argument.target.isBoolean()) {
                        value = "true";
                    } else {
                        if (i + 1 >= max) {
                            throw new ArgumentException("missing value for option " + arg);
                        }
                        i++;
                        value = args.get(i);
                    }
                }
                add(argument, value);
            } else {
                inOptions = false;
                if ("\\".equals(arg)) {
                    // ignore \ arguments - they are used to force switching from option- to value mode
                    continue;
                }
                if (position >= values.size()) {
                    builder = new StringBuilder("unknown value(s):");
                    for ( ; i < max; i++) {
                        builder.append(' ');
                        builder.append(args.get(i));
                    }
                    throw new ArgumentException(builder.toString());
                }
                argument = values.get(position);
                value = arg;
                if (add(argument, value)) {
                    position++;
                }
            }
        }

    }
}
