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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility methods shared across MCP project generators.
 */
public class GeneratorUtils {

    private GeneratorUtils() {
    }

    /**
     * Derives a valid Ballerina package name from an API title.
     * Converts to lowercase, replaces non-alphanumeric runs with {@code _},
     * strips leading/trailing underscores, and appends {@code _mcp}.
     */
    public static String derivePackageName(String title) {
        if (title == null || title.isBlank()) {
            return "mcp_server";
        }
        String name = title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
        if (name.isEmpty()) {
            return "mcp_server";
        }
        if (Character.isDigit(name.charAt(0))) {
            name = "_" + name;
        }
        return name + "_mcp";
    }

    public static void createDirectory(Path dir) throws McpGenerationException {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new McpGenerationException("Failed to create output directory: " + dir, e);
        }
    }

    public static void writeFile(Path path, String content) throws McpGenerationException {
        try {
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new McpGenerationException("Failed to write file: " + path, e);
        }
    }
}
