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

import java.nio.file.Path;

/**
 * Immutable value object that carries all options required by the MCP project generator.
 */
public final class GeneratorOptions {

    private final Path inputPath;
    private final Path outputPath;
    private final String inputType;

    /**
     * Constructs a new {@code GeneratorOptions} instance.
     *
     * @param inputPath  absolute path to the OpenAPI/Swagger contract file
     * @param outputPath absolute path to the output directory
     * @param inputType  contract format identifier (e.g. "openapi")
     */
    public GeneratorOptions(Path inputPath, Path outputPath, String inputType) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.inputType = inputType;
    }

    /**
     * Returns the absolute path to the input contract file.
     *
     * @return input file path
     */
    public Path getInputPath() {
        return inputPath;
    }

    /**
     * Returns the absolute path to the output directory.
     *
     * @return output directory path
     */
    public Path getOutputPath() {
        return outputPath;
    }

    /**
     * Returns the input contract type identifier.
     *
     * @return input type string (e.g. "openapi")
     */
    public String getInputType() {
        return inputType;
    }
}
