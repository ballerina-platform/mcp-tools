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

/**
 * Utility class for sanitizing identifiers to be valid Ballerina names.
 *
 * <p>Mirrors the {@code sanitizeTypeName} and {@code safeIdent} jq functions
 * from the reference generation script.
 */
public final class NameSanitizer {

    private NameSanitizer() {
        // utility class
    }

    /**
     * Sanitizes a schema / type name to be a valid Ballerina type identifier.
     *
     * <ol>
     *   <li>Replaces any non-alphanumeric/underscore character with {@code _}</li>
     *   <li>Prefixes with {@code _} if the name starts with a digit</li>
     *   <li>Appends {@code _} if the name is a reserved Ballerina keyword</li>
     * </ol>
     *
     * @param name the original schema name
     * @return a valid Ballerina type identifier
     */
    public static String sanitizeTypeName(String name) {
        if (name == null || name.isEmpty()) {
            return "_";
        }
        // Replace non-alphanumeric (excluding _) with _
        String sanitized = name.replaceAll("[^a-zA-Z0-9_]", "_");

        // Prefix with _ if starts with a digit
        if (Character.isDigit(sanitized.charAt(0))) {
            sanitized = "_" + sanitized;
        }

        // Append _ if it is a reserved keyword
        if (BallerinaKeywords.isKeyword(sanitized)) {
            sanitized = sanitized + "_";
        }

        return sanitized;
    }

    /**
     * Converts a property name to a safe Ballerina field identifier using camelCase.
     *
     * <ol>
     *   <li>Splits on non-alphanumeric characters</li>
     *   <li>camelCases the resulting segments</li>
     *   <li>Prefixes with {@code _} if starts with a digit</li>
     *   <li>Appends {@code _} if the result is a reserved Ballerina keyword</li>
     * </ol>
     *
     * @param name the original property name
     * @return a valid Ballerina field identifier
     */
    public static String safeIdent(String name) {
        if (name == null || name.isEmpty()) {
            return "field";
        }

        // Split on non-alphanumeric characters
        String[] parts = name.split("[^a-zA-Z0-9]+");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            if (i == 0) {
                sb.append(part);
            } else {
                // Capitalise first letter for camelCase
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1));
            }
        }

        String result = sb.toString();
        if (result.isEmpty()) {
            return "field";
        }

        // Prefix with _ if starts with a digit
        if (Character.isDigit(result.charAt(0))) {
            result = "_" + result;
        }

        // Append _ if it is a reserved keyword
        if (BallerinaKeywords.isKeyword(result)) {
            result = result + "_";
        }

        return result;
    }

    /**
     * Builds a tool/function name from an HTTP method and path when no operationId is available.
     *
     * <p>e.g. {@code GET /pets/{petId}} → {@code getPets_petId_}
     *
     * @param method HTTP method (get, post, put, delete, patch)
     * @param path   OpenAPI path string
     * @return camelCase function name
     */
    public static String buildToolName(String method, String path) {
        String[] segments = path.split("/");
        StringBuilder sb = new StringBuilder(method.toLowerCase());
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            // Strip path parameter braces and capitalise
            String cleaned = segment.replaceAll("[{}]", "");
            if (!cleaned.isEmpty()) {
                sb.append(Character.toUpperCase(cleaned.charAt(0)));
                sb.append(cleaned.substring(1));
            }
        }
        // Remove any remaining non-alphanumeric characters
        return sb.toString().replaceAll("[^a-zA-Z0-9_]", "");
    }
}
