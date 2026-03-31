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

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        name = "mcp",
        description = "Generate a Ballerina MCP server project from an API contract."
)
public class McpCmd implements BLauncherCmd {

    private static final String CMD_NAME = "mcp";
    private static final PrintStream OUT = System.out;
    private static final PrintStream ERR = System.err;

    @CommandLine.Option(
            names = {"-i", "--input"},
            description = "Path to the input API contract file (OpenAPI/Swagger YAML or JSON).",
            required = true
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

    private CommandLine parentCmdParser;

    @Override
    public void execute() {
        if (helpFlag) {
            String usageInfo = getUsageInfo();
            OUT.println(usageInfo);
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
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Generate a Ballerina MCP (Model Context Protocol) server project").append(System.lineSeparator());
        out.append("from a given API contract file.").append(System.lineSeparator());
        out.append(System.lineSeparator());
        out.append("The generated project includes:").append(System.lineSeparator());
        out.append("  - Ballerina.toml   : Project configuration").append(System.lineSeparator());
        out.append("  - main.bal         : MCP service with remote functions per API endpoint")
                .append(System.lineSeparator());
        out.append("  - types.bal        : Ballerina record types from API schemas").append(System.lineSeparator());
        out.append("  - README.md        : Project documentation").append(System.lineSeparator());
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal mcp -i <openapi-contract> -o <output-dir> --input-type openapi")
                .append(System.lineSeparator());
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    private String getUsageInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("NAME").append(System.lineSeparator());
        sb.append("    bal mcp - Generate a Ballerina MCP server from an API contract")
                .append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("SYNOPSIS").append(System.lineSeparator());
        printUsage(sb);
        sb.append(System.lineSeparator());
        sb.append("DESCRIPTION").append(System.lineSeparator());
        printLongDesc(sb);
        sb.append(System.lineSeparator());
        sb.append("OPTIONS").append(System.lineSeparator());
        sb.append("  -i, --input          Path to the input API contract file").append(System.lineSeparator());
        sb.append("  -o, --output         Output directory (default: current directory)").append(System.lineSeparator());
        sb.append("  --input-type         Contract format: openapi (default: openapi)").append(System.lineSeparator());
        sb.append("  -h, --help           Show this help message").append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("EXAMPLES").append(System.lineSeparator());
        sb.append("  bal mcp -i ./petstore.yaml -o ./petstore-mcp --input-type openapi")
                .append(System.lineSeparator());
        sb.append("  bal mcp -i ./api.json -o ./my-mcp-server --input-type openapi")
                .append(System.lineSeparator());
        return sb.toString();
    }
}
