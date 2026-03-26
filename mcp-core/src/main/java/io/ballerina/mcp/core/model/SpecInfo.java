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
import java.util.Map;

/**
 * Holds all parsed information from an OpenAPI/Swagger specification
 * that is needed to generate a Ballerina MCP server project.
 */
public final class SpecInfo {

    private final String baseUrl;
    private final int port;
    private final String title;
    private final String version;
    private final List<EndpointInfo> endpoints;
    private final Map<String, SchemaInfo> schemas;

    public SpecInfo(String baseUrl, int port, String title, String version,
                List<EndpointInfo> endpoints, Map<String, SchemaInfo> schemas) {
        this.baseUrl = baseUrl;
        this.port = port;
        this.title = title;
        this.version = version;
        this.endpoints = endpoints;
        this.schemas = schemas;
    }

    public String getBaseUrl() { return baseUrl; }
    public int getPort() { return port; }
    public String getTitle() { return title; }
    public String getVersion() { return version; }
    public List<EndpointInfo> getEndpoints() { return endpoints; }
    public Map<String, SchemaInfo> getSchemas() { return schemas; }
}
