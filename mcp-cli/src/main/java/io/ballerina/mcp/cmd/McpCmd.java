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

package io.ballerina.mcp.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.mcp.core.generator.GeneratorOptions;
import io.ballerina.mcp.core.generator.McpProjectGenerator;
import io.ballerina.mcp.core.utils.DiagnosticCode;
import io.ballerina.mcp.core.utils.DiagnosticLog;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Main CLI command for the Ballerina MCP tool.
 * Registered as a top-level {@code bal mcp} sub-command via Java SPI.
 *
 * <p>Usage:
 * <pre>
 *   bal mcp -i &lt;openapi-contract&gt; -o &lt;output-dir&gt; --input-type openapi
 * </pre>
 */
@CommandLine.Command(
        name = McpCmd.TOOL_NAME,
        description = "Generate a Ballerina MCP server project from an API contract."
)
public class McpCmd implements BLauncherCmd {

    static final String TOOL_NAME = "mcp";
    private static final PrintStream OUT = System.out;
    private static final PrintStream ERR = System.err;

    @CommandLine.Option(
            names = {"-i", "--input"},
            description = "Path to the input API contract file (OpenAPI/Swagger YAML or JSON)."
    )
    private String inputPath;

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Output directory for the generated Ballerina MCP server project.",
            defaultValue = "."
    )
    private String outputPath;

    @CommandLine.Option(
            names = {"--input-type"},
            description = "Type of the input contract. Supported values: openapi (default: openapi).",
            defaultValue = "openapi"
    )
    private String inputType;

    @CommandLine.Option(
            names = {"--help", "-h"},
            usageHelp = true,
            hidden = true
    )
    private boolean helpFlag;


    @Override
    public void execute() {
        if (helpFlag) {
            OUT.println(getHelpText());
            return;
        }

        if (inputPath == null) {
            OUT.println(getHelpText());
            Runtime.getRuntime().exit(1);
            return;
        }

        // Validate --input-type
        if (!"openapi".equalsIgnoreCase(inputType)) {
            ERR.println(DiagnosticLog.error(DiagnosticCode.UNSUPPORTED_INPUT_TYPE, inputType));
            Runtime.getRuntime().exit(1);
            return;
        }

        Path input = Paths.get(inputPath).toAbsolutePath().normalize();
        Path output = Paths.get(outputPath).toAbsolutePath().normalize();

        if (!input.toFile().exists()) {
            ERR.println(DiagnosticLog.error(DiagnosticCode.INPUT_FILE_NOT_FOUND, input));
            Runtime.getRuntime().exit(1);
            return;
        }

        OUT.println("Generating Ballerina MCP server from: " + input);

        try {
            GeneratorOptions options = new GeneratorOptions(input, output, inputType);
            McpProjectGenerator generator = new McpProjectGenerator(options);
            generator.generate();
            OUT.println("MCP server project generated successfully at: " + output);
        } catch (Exception e) {
            ERR.println(DiagnosticLog.error(DiagnosticCode.MCP_GENERATION_FAILED, e.getMessage()));
            Runtime.getRuntime().exit(1);
        }
    }

    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
    }

    @Override
    public void printUsage(StringBuilder out) {
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private String getHelpText() {
        try (InputStream inputStream = McpCmd.class.getClassLoader()
                .getResourceAsStream("ballerina-" + TOOL_NAME + ".help");
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "help text not found";
        }
    }
}
