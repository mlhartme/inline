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

/** Indicates a problem with the arguments supplied. Thrown when using the cli.  */
public class ArgumentException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ArgumentException(String msg) {
        super(msg);
    }
    public ArgumentException(String msg, Throwable cause) {
        this(msg);
        initCause(cause);
    }
}
