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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates the content of {@code main.bal} for a Ballerina MCP server project.
 */
public class MainBalGenerator {

    private static final String NL = "\n";
    private static final String INDENT = "    ";

    /**
     * Generates the full content of {@code main.bal}.
     *
     * @param spec the parsed spec information
     * @return the generated Ballerina source as a string
     * @throws McpGenerationException if the template cannot be loaded
     */
    public String generate(SpecInfo spec) throws McpGenerationException {
        try {
            String port = spec.getPort() > 0 ? String.valueOf(spec.getPort()) : Constants.DEFAULT_MCP_PORT;
            String servicePath = deriveServicePath(spec.getTitle());
            String remoteFunctions = buildRemoteFunctions(spec.getEndpoints());

            return TemplateLoader.load("main.bal")
                    .replace("{{BASE_URL}}", spec.getBaseUrl())
                    .replace("{{PORT}}", port)
                    .replace("{{TITLE}}", escapeString(spec.getTitle()))
                    .replace("{{VERSION}}", escapeString(spec.getVersion() != null ? spec.getVersion() : ""))
                    .replace("{{SERVICE_PATH}}", servicePath)
                    .replace("{{REMOTE_FUNCTIONS}}", remoteFunctions);
        } catch (IOException e) {
            throw new McpGenerationException("Failed to load main.bal template: " + e.getMessage(), e);
        }
    }

    private String deriveServicePath(String title) {
        String path = title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
        return path.isEmpty() ? Constants.DEFAULT_SERVICE_PATH : path;
    }

    private String buildRemoteFunctions(List<EndpointInfo> endpoints) {
        StringBuilder sb = new StringBuilder();
        for (EndpointInfo endpoint : endpoints) {
            appendRemoteFunction(sb, endpoint);
        }
        return sb.toString();
    }

    private void appendRemoteFunction(StringBuilder sb, EndpointInfo endpoint) {
        sb.append(NL);

        sb.append(INDENT).append("@mcp:Tool {").append(NL);
        sb.append(INDENT).append(INDENT).append("description: \"")
                .append(escapeString(endpoint.getDescription())).append("\"").append(NL);
        sb.append(INDENT).append("}").append(NL);

        sb.append(INDENT).append("remote function ").append(endpoint.getToolName()).append("(");
        sb.append(buildArgList(endpoint));
        sb.append(") returns ").append(endpoint.getReturnType()).append("|error {").append(NL);

        sb.append(INDENT).append(INDENT)
                .append("log:printInfo(\"Proxying request to: \" + string `")
                .append(endpoint.getBalPath()).append("`);").append(NL);

        sb.append(INDENT).append(INDENT)
                .append(endpoint.getReturnType()).append(" response = check apiClient->")
                .append(endpoint.getMethod()).append("(string `")
                .append(endpoint.getBalPath()).append("`");

        if (endpoint.hasBody()) {
            sb.append(", payload");
        } else if (endpoint.getMethod().equals(Constants.HTTP_POST)
                || endpoint.getMethod().equals(Constants.HTTP_PUT)
                || endpoint.getMethod().equals(Constants.HTTP_PATCH)) {
            sb.append(", {}");
        }

        if (endpoint.hasQueryParams()) {
            sb.append(", queries = {");
            List<String> queryEntries = new ArrayList<>();
            for (ParameterInfo qp : endpoint.getQueryParameters()) {
                queryEntries.add("\"" + qp.getOriginalName() + "\": " + qp.getSafeName());
            }
            sb.append(String.join(", ", queryEntries));
            sb.append("}");
        }

        sb.append(");").append(NL);
        sb.append(INDENT).append(INDENT).append("return response;").append(NL);
        sb.append(INDENT).append("}").append(NL);
    }

    private String buildArgList(EndpointInfo endpoint) {
        List<String> args = new ArrayList<>();
        for (ParameterInfo param : endpoint.getParameters()) {
            args.add(param.toArgDeclaration());
        }
        for (ParameterInfo param : endpoint.getQueryParameters()) {
            args.add(param.toArgDeclaration());
        }
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
