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
                    result.add(str.substring(idx, max));
                }
                return result;
            }
            result.add(str.substring(idx, next));
            idx = next + 1;
        }
    }
}
