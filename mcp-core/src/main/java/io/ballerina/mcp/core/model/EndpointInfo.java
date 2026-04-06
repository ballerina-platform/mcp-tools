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

package io.ballerina.mcp.core.model;

import java.util.List;

/**
 * Represents a single API endpoint (HTTP method + path) and all the
 * information needed to generate its Ballerina {@code remote function}.
 */
public final class EndpointInfo {

    private final String path;
    private final String balPath;
    private final String method;
    private final String toolName;
    private final String description;
    private final List<ParameterInfo> parameters;
    private final List<ParameterInfo> queryParameters;
    private final String bodyType;
    private final String returnType;

    public EndpointInfo(String path, String balPath, String method, String toolName,
                        String description, List<ParameterInfo> parameters, List<ParameterInfo> queryParameters,
                        String bodyType, String returnType) {
        this.path = path;
        this.balPath = balPath;
        this.method = method;
        this.toolName = toolName;
        this.description = description;
        this.parameters = parameters;
        this.queryParameters = queryParameters;
        this.bodyType = bodyType;
        this.returnType = returnType;
    }

    /** Original OpenAPI path string (e.g. {@code /pets/{petId}}). */
    public String getPath() { return path; }

    /**
     * Ballerina string-template path with {@code ${param}} substitutions
     * (e.g. {@code /pets/${petId}}).
     */
    public String getBalPath() { return balPath; }

    /** HTTP method in lowercase (get, post, put, delete, patch). */
    public String getMethod() { return method; }

    /** Generated or extracted function/tool name. */
    public String getToolName() { return toolName; }

    /** Human-readable description for the {@code @mcp:Tool} annotation. */
    public String getDescription() { return description; }

    /** List of path parameters with their Ballerina types. */
    public List<ParameterInfo> getParameters() { return parameters; }

    /** List of query parameters with their Ballerina types. */
    public List<ParameterInfo> getQueryParameters() { return queryParameters; }

    /**
     * Ballerina type of the request body, or {@code null} if no body.
     */
    public String getBodyType() { return bodyType; }

    /** Ballerina return type derived from the first 2xx response. */
    public String getReturnType() { return returnType; }

    /** Returns {@code true} if this endpoint has a request body. */
    public boolean hasBody() { return bodyType != null; }

    /** Returns {@code true} if this endpoint has query parameters. */
    public boolean hasQueryParams() { return queryParameters != null && !queryParameters.isEmpty(); }
}
