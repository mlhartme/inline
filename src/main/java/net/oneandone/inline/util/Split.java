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

import net.oneandone.inline.internal.InvalidCliException;

import java.util.ArrayList;
import java.util.List;

public class Split {
    public static List<String> split(String str) {
        List<String> result;
        int idx;
        int max;
        int next;

        result = new ArrayList<>();
        idx = 0;
        max = str.length();
        while (true) {
            while (idx < max && str.charAt(idx) == ' ') {
                idx++;
            }
            next = str.indexOf(' ', idx);
            if (next == -1) {
                if (idx < max) {
                    result.add(escape(str.substring(idx, max)));
                }
                return result;
            }
            result.add(escape(str.substring(idx, next)));
            idx = next + 1;
        }
    }

    public static String escape(String str) {
        int prev;
        int idx;
        StringBuilder result;

        idx = str.indexOf('§');
        if (idx == -1) {
            return str;
        }
        prev = 0;
        result = new StringBuilder(str.length());
        do {
            result.append(str.substring(prev, idx));
            if (idx + 3 > str.length()) {
                throw new InvalidCliException("invalid $ constant in " + str);
            }
            result.append((char) Integer.parseInt(str.substring(idx + 1, idx + 3), 16));
            prev = idx + 3;
            idx = str.indexOf('§', prev);
        } while (idx != -1);
        result.append(str.substring(prev));
        return result.toString();
    }
}
