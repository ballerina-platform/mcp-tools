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

import io.ballerina.mcp.core.model.SpecInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Orchestrates the full MCP project generation pipeline.
 *
 * <ol>
 *   <li>Parse the input OpenAPI spec → {@link SpecInfo}</li>
 *   <li>Derive package name and output directory</li>
 *   <li>Generate and write {@code main.bal}</li>
 *   <li>Generate and write {@code types.bal} (if schemas exist)</li>
 *   <li>Generate and write {@code Ballerina.toml}</li>
 *   <li>Generate and write {@code README.md}</li>
 * </ol>
 */
public class McpProjectGenerator {

    private final GeneratorOptions options;

    private final OpenApiSpecParser specParser = new OpenApiSpecParser();
    private final MainBalGenerator mainBalGenerator = new MainBalGenerator();
    private final TypesBalGenerator typesBalGenerator = new TypesBalGenerator();
    private final BallerinaTomlGenerator tomlGenerator = new BallerinaTomlGenerator();
    private final ReadmeGenerator readmeGenerator = new ReadmeGenerator();

    public McpProjectGenerator(GeneratorOptions options) {
        this.options = options;
    }

    /**
     * Runs the full generation pipeline.
     *
     * @throws McpGenerationException if parsing or file I/O fails
     */
    public void generate() throws McpGenerationException {
        // 1. Parse spec
        SpecInfo spec = specParser.parse(options.getInputPath());

        // 2. Derive package name and project output directory
        String packageName = derivePackageName(spec.getTitle());
        Path projectDir = options.getOutputPath().resolve(packageName);

        createDirectory(projectDir);

        // 3. Write main.bal
        String mainBal = mainBalGenerator.generate(spec);
        writeFile(projectDir.resolve("main.bal"), mainBal);

        // 4. Write types.bal (only if there are schemas)
        String typesBal = typesBalGenerator.generate(spec);
        if (typesBal != null) {
            writeFile(projectDir.resolve("types.bal"), typesBal);
        }

        // 5. Write Ballerina.toml
        String toml = tomlGenerator.generate(spec, packageName);
        writeFile(projectDir.resolve("Ballerina.toml"), toml);

        // 6. Write README.md
        String readme = readmeGenerator.generate(spec);
        writeFile(projectDir.resolve("README.md"), readme);

        System.out.println("Generated files:");
        System.out.println("  " + projectDir.resolve("main.bal"));
        if (typesBal != null) {
            System.out.println("  " + projectDir.resolve("types.bal"));
        }
        System.out.println("  " + projectDir.resolve("Ballerina.toml"));
        System.out.println("  " + projectDir.resolve("README.md"));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Derives a valid Ballerina package name from an API title.
     * Converts to lowercase, replaces non-alphanumeric runs with {@code _},
     * strips leading/trailing underscores, and appends {@code _mcp}.
     *
     * @param title API title from the spec
     * @return a valid Ballerina package name
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
        // Prefix with _ if starts with digit
        if (Character.isDigit(name.charAt(0))) {
            name = "_" + name;
        }
        return name + "_mcp";
    }

    private void createDirectory(Path dir) throws McpGenerationException {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new McpGenerationException("Failed to create output directory: " + dir, e);
        }
    }

    private void writeFile(Path path, String content) throws McpGenerationException {
        try {
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new McpGenerationException("Failed to write file: " + path, e);
        }
    }
}
