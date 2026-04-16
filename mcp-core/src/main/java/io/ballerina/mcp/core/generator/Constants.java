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

/**
 * Shared string constants used across MCP project generators.
 */
public class Constants {

    private Constants() {
    }

    // HTTP methods
    public static final String HTTP_GET = "get";
    public static final String HTTP_POST = "post";
    public static final String HTTP_PUT = "put";
    public static final String HTTP_DELETE = "delete";
    public static final String HTTP_PATCH = "patch";

    // Delimiters
    public static final String DELIMITER = ":";

    // OpenAPI parameter locations
    public static final String PARAM_IN_PATH = "path";
    public static final String PARAM_IN_QUERY = "query";

    // Content types
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    // Ballerina type names
    public static final String BAL_TYPE_JSON = "json";
    public static final String BAL_TYPE_STRING = "string";

    // OAS primitive type names
    public static final String OAS_TYPE_ARRAY = "array";
    public static final String OAS_TYPE_INTEGER = "integer";
    public static final String OAS_TYPE_NUMBER = "number";
    public static final String OAS_TYPE_BOOLEAN = "boolean";

    // Default values
    public static final String DEFAULT_SPEC_VERSION = "1.0.0";
    public static final String DEFAULT_VERSION = "0.1.0";
    public static final String DEFAULT_PACKAGE_NAME = "mcp_server";
    public static final String DEFAULT_ORG_NAME = "my_org";
    public static final String DEFAULT_MCP_PORT = "9090";
    public static final String DEFAULT_SERVICE_PATH = "mcp";
    public static final String DEFAULT_TOOL_NAME = "operation";
}
