/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.mcp.core.generator;

import java.util.Set;

/**
 * Ballerina reserved keywords used to sanitize generated identifiers.
 * Matches the keyword list used in the reference generation script.
 */
public final class BallerinaKeywords {

    private BallerinaKeywords() {
        // utility class
    }

    /**
     * Complete set of Ballerina reserved keywords and built-in type names.
     */
    public static final Set<String> KEYWORDS = Set.of(
            "abort", "abstract", "all", "annotation", "any", "anydata",
            "boolean", "break", "byte",
            "check", "checkpanic", "class", "client", "commit", "const", "continue",
            "decimal", "distinct", "do",
            "else", "enum", "error", "external",
            "fail", "false", "final", "float", "flush", "fork", "function", "future",
            "handle", "if", "import", "in", "int", "intersection", "isolated",
            "join", "json",
            "limit", "listener", "lock",
            "map", "match", "module",
            "never", "new", "null",
            "object", "on", "order",
            "panic", "parameterized", "public",
            "readonly", "record", "remote", "resource", "retry", "return", "rollback",
            "select", "service", "source", "start", "stream", "string",
            "table", "transaction", "transactional", "trap", "true", "type", "typedesc",
            "union",
            "var", "version",
            "wait", "where", "while", "worker",
            "xml"
    );
}
