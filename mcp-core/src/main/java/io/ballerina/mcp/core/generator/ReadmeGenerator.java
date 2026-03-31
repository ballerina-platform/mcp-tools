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
import io.ballerina.mcp.core.model.SpecInfo;

import java.io.IOException;

/**
 * Generates the content of {@code README.md} for a Ballerina MCP server project.
 */
public class ReadmeGenerator {

    /**
     * Generates the full content of {@code README.md}.
     *
     * @param spec the parsed spec information
     * @return the generated Markdown content as a string
     * @throws McpGenerationException if the template cannot be loaded
     */
    public String generate(SpecInfo spec) throws McpGenerationException {
        try {
            String version = spec.getVersion() != null ? spec.getVersion() : "";
            return TemplateLoader.load("README.md")
                    .replace("{{TITLE}}", spec.getTitle())
                    .replace("{{VERSION}}", version)
                    .replace("{{BASE_URL}}", spec.getBaseUrl())
                    .replace("{{TOOLS_TABLE}}", buildToolsTable(spec));
        } catch (IOException e) {
            throw new McpGenerationException("Failed to load README.md template: " + e.getMessage(), e);
        }
    }

    private String buildToolsTable(SpecInfo spec) {
        StringBuilder sb = new StringBuilder();
        sb.append("| Function | Method | Path | Description |\n");
        sb.append("|---|---|---|---|\n");
        for (EndpointInfo endpoint : spec.getEndpoints()) {
            sb.append("| `").append(endpoint.getToolName()).append("` | ")
                    .append(endpoint.getMethod().toUpperCase()).append(" | `")
                    .append(endpoint.getPath()).append("` | ")
                    .append(sanitizeDescription(endpoint.getDescription())).append(" |\n");
        }
        return sb.toString();
    }

    private String sanitizeDescription(String description) {
        if (description == null) {
            return "";
        }
        return description
                .replaceAll("\\r?\\n", " ")
                .replace("|", "\\|")
                .trim();
    }
}
