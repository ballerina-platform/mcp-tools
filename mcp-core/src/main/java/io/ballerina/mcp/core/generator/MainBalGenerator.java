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
    private static final String RETURN_DOC = "The API response, or an error if the request fails";

    /**
     * Generates the full content of {@code main.bal}.
     *
     * @param spec the parsed spec information
     * @return the generated Ballerina source as a string
     * @throws McpGenerationException if the template cannot be loaded
     */
    public String generate(SpecInfo spec) throws McpGenerationException {
        try {
            String port = Constants.DEFAULT_MCP_PORT;
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

    private String buildPathWithQuery(EndpointInfo endpoint) {
        StringBuilder path = new StringBuilder(endpoint.getBalPath());
        if (endpoint.hasQueryParams()) {
            path.append("?");
            List<String> queryParts = new ArrayList<>();
            for (ParameterInfo qp : endpoint.getQueryParameters()) {
                queryParts.add(qp.getOriginalName() + "=${" + qp.getSafeName() + "}");
            }
            path.append(String.join("&", queryParts));
        }
        return path.toString();
    }

    private void appendRemoteFunction(StringBuilder sb, EndpointInfo endpoint) {
        sb.append(NL);

        appendDocComment(sb, endpoint);

        sb.append(INDENT).append("isolated remote function ").append(endpoint.getToolName()).append("(");
        sb.append(buildArgList(endpoint));
        sb.append(") returns ").append(endpoint.getReturnType()).append("|error {").append(NL);

        String pathWithQuery = buildPathWithQuery(endpoint);

        sb.append(INDENT).append(INDENT)
                .append("log:printInfo(\"Proxying request to: \" + string `")
                .append(pathWithQuery).append("`);").append(NL);

        sb.append(INDENT).append(INDENT)
                .append(endpoint.getReturnType()).append(" response = check apiClient->")
                .append(endpoint.getMethod()).append("(string `")
                .append(pathWithQuery).append("`");

        if (endpoint.hasBody()) {
            sb.append(", payload");
        } else if (endpoint.getMethod().equals(Constants.HTTP_POST)
                || endpoint.getMethod().equals(Constants.HTTP_PUT)
                || endpoint.getMethod().equals(Constants.HTTP_PATCH)) {
            sb.append(", {}");
        }

        sb.append(");").append(NL);
        sb.append(INDENT).append(INDENT).append("return response;").append(NL);
        sb.append(INDENT).append("}").append(NL);
    }

    /**
     * Writes the tool's doc comment. The mcp.v2 compiler plugin publishes the description as the tool
     * description and each {@code + name - text} line as that argument's description in the input schema.
     * Every argument and the return value get a line, even an empty one, so the build reports no
     * undocumented-parameter warnings.
     */
    private void appendDocComment(StringBuilder sb, EndpointInfo endpoint) {
        for (String line : endpoint.getDescription().strip().split("\\R", -1)) {
            appendDocLine(sb, escapeDocLine(line.stripTrailing()));
        }
        appendDocLine(sb, "");
        for (ParameterInfo param : endpoint.getParameters()) {
            appendParamDoc(sb, param.getSafeName(), param.getDescription());
        }
        for (ParameterInfo param : endpoint.getQueryParameters()) {
            appendParamDoc(sb, param.getSafeName(), param.getDescription());
        }
        if (endpoint.hasBody()) {
            appendParamDoc(sb, "payload", endpoint.getBodyDescription());
        }
        appendParamDoc(sb, "return", RETURN_DOC);
    }

    private void appendParamDoc(StringBuilder sb, String name, String description) {
        String text = description == null ? "" : description.strip().replaceAll("\\s+", " ");
        appendDocLine(sb, text.isEmpty() ? "+ " + name + " -" : "+ " + name + " - " + text);
    }

    private void appendDocLine(StringBuilder sb, String text) {
        sb.append(INDENT).append("#");
        if (!text.isEmpty()) {
            sb.append(" ").append(text);
        }
        sb.append(NL);
    }

    /**
     * Ballerina reads any doc line that starts with {@code +} as a parameter doc, which would end the
     * description early. A {@code + } list bullet becomes the equivalent {@code - } bullet; any other leading
     * {@code +} gets a no-break space in front, which the doc parser treats as text.
     */
    private String escapeDocLine(String line) {
        String trimmed = line.stripLeading();
        if (!trimmed.startsWith("+")) {
            return line;
        }
        String indent = line.substring(0, line.length() - trimmed.length());
        if (trimmed.length() == 1 || Character.isWhitespace(trimmed.charAt(1))) {
            return indent + "-" + trimmed.substring(1);
        }
        return indent + "\u00A0" + trimmed;
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
