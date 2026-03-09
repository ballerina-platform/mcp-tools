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

import io.ballerina.mcp.core.model.EndpointInfo;
import io.ballerina.mcp.core.model.ParameterInfo;
import io.ballerina.mcp.core.model.SpecInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates the content of {@code main.bal} for a Ballerina MCP server project.
 *
 * <p>Output structure:
 * <pre>
 *   imports
 *   http:Client declaration
 *   mcp:Listener declaration
 *   @mcp:ServiceConfig annotation
 *   service mcp:Service /&lt;servicePath&gt; on mcpListener {
 *       // one remote function per endpoint
 *   }
 * </pre>
 */
public class MainBalGenerator {

    private static final String NL = System.lineSeparator();
    private static final String INDENT = "    ";

    /**
     * Generates the full content of {@code main.bal}.
     *
     * @param spec the parsed spec information
     * @return the generated Ballerina source as a string
     */
    public String generate(SpecInfo spec) {
        StringBuilder sb = new StringBuilder();

        appendImports(sb);
        appendClientAndListener(sb, spec.getBaseUrl());
        appendServiceConfig(sb, spec.getTitle(), spec.getVersion());
        appendServiceBlock(sb, spec);

        return sb.toString();
    }

    // -----------------------------------------------------------------------

    private void appendImports(StringBuilder sb) {
        sb.append("import ballerina/log;").append(NL);
        sb.append("import ballerina/mcp;").append(NL);
        sb.append("import ballerina/http;").append(NL);
        sb.append(NL);
    }

    private void appendClientAndListener(StringBuilder sb, String baseUrl) {
        sb.append("http:Client apiClient = check new (\"").append(baseUrl).append("\");").append(NL);
        sb.append("listener mcp:Listener mcpListener = check new (9090);").append(NL);
        sb.append(NL);
    }

    private void appendServiceConfig(StringBuilder sb, String title, String version) {
        sb.append("@mcp:ServiceConfig {").append(NL);
        sb.append(INDENT).append("info: {").append(NL);
        sb.append(INDENT).append(INDENT).append("name: \"").append(escapeString(title)).append("\",").append(NL);
        sb.append(INDENT).append(INDENT).append("version: \"").append(escapeString(version)).append("\"").append(NL);
        sb.append(INDENT).append("}").append(NL);
        sb.append("}").append(NL);
    }

    private void appendServiceBlock(StringBuilder sb, SpecInfo spec) {
        // Derive service path from title (lowercase, spaces → underscores)
        String servicePath = spec.getTitle()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
        if (servicePath.isEmpty()) {
            servicePath = "mcp";
        }

        sb.append("service mcp:Service /").append(servicePath).append(" on mcpListener {").append(NL);

        for (EndpointInfo endpoint : spec.getEndpoints()) {
            appendRemoteFunction(sb, endpoint);
        }

        sb.append("}").append(NL);
    }

    private void appendRemoteFunction(StringBuilder sb, EndpointInfo endpoint) {
        sb.append(NL);

        // @mcp:Tool annotation
        sb.append(INDENT).append("@mcp:Tool {").append(NL);
        sb.append(INDENT).append(INDENT).append("description: \"")
                .append(escapeString(endpoint.getDescription())).append("\"").append(NL);
        sb.append(INDENT).append("}").append(NL);

        // Function signature
        sb.append(INDENT).append("remote function ").append(endpoint.getToolName()).append("(");
        sb.append(buildArgList(endpoint));
        sb.append(") returns ").append(endpoint.getReturnType()).append("|error {").append(NL);

        // Function body
        sb.append(INDENT).append(INDENT)
                .append("log:printInfo(\"Proxying request to: \" + string `")
                .append(endpoint.getBalPath()).append("`);").append(NL);

        sb.append(INDENT).append(INDENT)
                .append(endpoint.getReturnType()).append(" response = check apiClient->")
                .append(endpoint.getMethod()).append("(string `")
                .append(endpoint.getBalPath()).append("`");

        if (endpoint.hasBody()) {
            sb.append(", payload");
        }

        sb.append(");").append(NL);
        sb.append(INDENT).append(INDENT).append("return response;").append(NL);
        sb.append(INDENT).append("}").append(NL);
    }

    private String buildArgList(EndpointInfo endpoint) {
        List<String> args = new ArrayList<>();

        // Path parameters first
        for (ParameterInfo param : endpoint.getParameters()) {
            args.add(param.toArgDeclaration());
        }

        // Then body
        if (endpoint.hasBody()) {
            args.add(endpoint.getBodyType() + " payload");
        }

        return String.join(", ", args);
    }

    private String escapeString(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
