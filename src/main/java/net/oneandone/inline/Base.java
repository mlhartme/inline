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

import net.oneandone.inline.internal.InvalidCliException;
import net.oneandone.inline.internal.Mapping;

import java.util.ArrayList;
import java.util.List;

public class Base {
    public static Base create(Base parent, Class<?> base, String definition) {
        int idx;
        String syntax;
        String mapping;

        idx = definition.indexOf('{');
        if (idx == -1) {
            syntax = definition;
            mapping = "";
        } else {
            if (!definition.endsWith("}")) {
                throw new InvalidCliException(definition);
            }
            mapping = definition.substring(idx + 1, definition.length() - 1).trim();
            syntax = definition.substring(0, idx).trim();
        }
        Mapping.parse(mapping, base);
        return new Base(parent, syntax, mapping);

    }

    private final Base parent;
    private final String syntax;
    private final String mapping;

    public Base(Base parent, String syntax, String mapping) {
        this.parent = parent;
        this.syntax = syntax;
        this.mapping = mapping;
    }

    public String fullSyntax(String extraSyntax) {
        StringBuilder result;

        result = new StringBuilder();
        for (Base base : stack()) {
            result.append(base.syntax);
            result.append(' ');
        }
        result.append(extraSyntax);
        return result.toString();
    }

    public String fullMapping(String extraMapping) {
        StringBuilder result;

        result = new StringBuilder();
        for (Base base : stack()) {
            result.append(base.mapping);
            result.append(' ');
        }
        result.append(extraMapping);
        return result.toString();
    }

    private List<Base> stack() {
        List<Base> result;

        result = new ArrayList<>();
        stack(result);
        return result;
    }
    private void stack(List<Base> result) {
        if (parent != null) {
            parent.stack(result);
        }
        result.add(this);
    }
}
