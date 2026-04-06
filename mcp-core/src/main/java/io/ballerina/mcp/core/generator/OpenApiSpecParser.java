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
import io.ballerina.mcp.core.utils.DiagnosticCode;
import io.ballerina.mcp.core.utils.DiagnosticLog;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.UnsupportedOASDataTypeException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parses an OpenAPI 3.x specification and returns a {@link SpecInfo} containing all information
 * needed for Ballerina MCP code generation.
 *
 * <p>Name sanitization and type mapping delegate to
 * {@link GeneratorUtils} from the {@code io.ballerina.openapi:core} library so that the
 * generated identifiers are consistent with the rest of the Ballerina OpenAPI toolchain.
 */
public class OpenApiSpecParser {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    /** The raw OpenAPI model from the last successful {@link #parse(Path)} call. */
    private OpenAPI parsedOpenAPI;

    /**
     * Parses the spec file at the given path and returns a {@link SpecInfo}.
     *
     * @param specPath path to the OpenAPI YAML or JSON file
     * @return parsed spec information
     * @throws McpGenerationException if the file cannot be parsed
     */
    public SpecInfo parse(Path specPath) throws McpGenerationException {
        String fileName = specPath.getFileName().toString().toLowerCase();
        if (!fileName.endsWith(".yaml") && !fileName.endsWith(".yml") && !fileName.endsWith(".json")) {
            throw new McpGenerationException(
                    "Unsupported file type: '" + specPath.getFileName() + "'. " +
                    "Supported formats are: .yaml, .yml, .json");
        }

        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);

        SwaggerParseResult result = new OpenAPIV3Parser()
                .readLocation(specPath.toString(), null, parseOptions);

        if (result == null || result.getOpenAPI() == null) {
            String messages = result != null && result.getMessages() != null
                    ? String.join("; ", result.getMessages())
                    : "unknown error";
            throw new McpGenerationException("Failed to parse OpenAPI spec: " + messages);
        }

        OpenAPI openAPI = result.getOpenAPI();
        this.parsedOpenAPI = openAPI;

        String baseUrl = extractBaseUrl(openAPI);
        int port = extractPort(baseUrl);
        String title = openAPI.getInfo() != null ? openAPI.getInfo().getTitle() : "Proxy Service";
        String version = openAPI.getInfo() != null ? openAPI.getInfo().getVersion()
                : Constants.DEFAULT_SPEC_VERSION;

        List<EndpointInfo> endpoints = extractEndpoints(openAPI);

        return new SpecInfo(baseUrl, port, title, version, endpoints);
    }

    /**
     * Returns the raw {@link OpenAPI} model from the last successful {@link #parse(Path)} call.
     * Intended for use by {@code McpProjectGenerator} to drive type generation via
     * {@code TypeHandler}.
     */
    public OpenAPI getOpenAPI() {
        return parsedOpenAPI;
    }

    private String extractBaseUrl(OpenAPI openAPI) {
        if (openAPI.getServers() != null && !openAPI.getServers().isEmpty()) {
            Server server = openAPI.getServers().get(0);
            String url = server.getUrl();
            if (url == null || url.isBlank()) {
                return DEFAULT_BASE_URL;
            }
            if (server.getVariables() != null) {
                for (Map.Entry<String, ServerVariable> entry : server.getVariables().entrySet()) {
                    String placeholder = "{" + entry.getKey() + "}";
                    String defaultValue = entry.getValue().getDefault();
                    if (defaultValue != null) {
                        url = url.replace(placeholder, defaultValue);
                    }
                }
            }
            if (url.contains("{") && url.contains("}")) {
                System.err.println(DiagnosticLog.warning(
                        DiagnosticCode.UNRESOLVED_SERVER_VARIABLE, url, DEFAULT_BASE_URL));
                return DEFAULT_BASE_URL;
            }
            return url;
        }
        return DEFAULT_BASE_URL;
    }

    private int extractPort(String baseUrl) {
        try {
            java.net.URI uri = new java.net.URI(baseUrl);
            return uri.getPort();
        } catch (Exception e) {
            return -1;
        }
    }

    private List<EndpointInfo> extractEndpoints(OpenAPI openAPI) {
        List<EndpointInfo> endpoints = new ArrayList<>();
        if (openAPI.getPaths() == null) {
            return endpoints;
        }

        for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
            String path = pathEntry.getKey();
            PathItem pathItem = pathEntry.getValue();

            addEndpointIfPresent(endpoints, path, Constants.HTTP_GET,    pathItem.getGet(),    pathItem, openAPI);
            addEndpointIfPresent(endpoints, path, Constants.HTTP_POST,   pathItem.getPost(),   pathItem, openAPI);
            addEndpointIfPresent(endpoints, path, Constants.HTTP_PUT,    pathItem.getPut(),    pathItem, openAPI);
            addEndpointIfPresent(endpoints, path, Constants.HTTP_DELETE, pathItem.getDelete(), pathItem, openAPI);
            addEndpointIfPresent(endpoints, path, Constants.HTTP_PATCH,  pathItem.getPatch(),  pathItem, openAPI);
        }
        return endpoints;
    }

    private void addEndpointIfPresent(List<EndpointInfo> endpoints,
                                      String path, String method, Operation operation,
                                      PathItem pathItem, OpenAPI openAPI) {
        if (operation == null) {
            return;
        }

        String toolName;
        if (operation.getOperationId() != null && !operation.getOperationId().isBlank()) {
            String sanitized = GeneratorUtils.getValidName(operation.getOperationId(), false);
            toolName = sanitized.isEmpty() ? buildToolName(method, path) : sanitized;
        } else {
            toolName = buildToolName(method, path);
        }

        String description = operation.getSummary() != null ? operation.getSummary()
                : operation.getDescription() != null ? operation.getDescription()
                : "Executes " + method.toUpperCase() + " on " + path;

        Map<String, Parameter> mergedParams = new LinkedHashMap<>();
        if (pathItem.getParameters() != null) {
            for (Parameter param : pathItem.getParameters()) {
                Parameter resolved = resolveParameter(param, openAPI);
                if (resolved != null) {
                    mergedParams.put(resolved.getIn() + ":" + resolved.getName(), resolved);
                }
            }
        }
        if (operation.getParameters() != null) {
            for (Parameter param : operation.getParameters()) {
                Parameter resolved = resolveParameter(param, openAPI);
                if (resolved != null) {
                    mergedParams.put(resolved.getIn() + ":" + resolved.getName(), resolved);
                }
            }
        }

        List<ParameterInfo> parameters = new ArrayList<>();
        List<ParameterInfo> queryParameters = new ArrayList<>();

        for (Parameter param : mergedParams.values()) {
            if (Constants.PARAM_IN_PATH.equals(param.getIn())) {
                String balType = schemaToBalType(param.getSchema());
                String safeName = GeneratorUtils.getValidName(param.getName(), false);
                parameters.add(new ParameterInfo(param.getName(), safeName, balType,
                        Constants.PARAM_IN_PATH));
            } else if (Constants.PARAM_IN_QUERY.equals(param.getIn())) {
                String balType = schemaToBalType(param.getSchema());
                String safeName = GeneratorUtils.getValidName(param.getName(), false);
                queryParameters.add(new ParameterInfo(param.getName(), safeName, balType,
                        Constants.PARAM_IN_QUERY));
            }
        }

        String bodyType = null;
        if (operation.getRequestBody() != null) {
            bodyType = extractRequestBodyType(operation.getRequestBody());
        }

        String returnType = extractReturnType(operation);
        String balPath = buildBalPath(path, parameters);

        endpoints.add(new EndpointInfo(
            path, balPath, method, toolName, description,
            parameters, queryParameters, bodyType, returnType));
    }

    private Parameter resolveParameter(Parameter param, OpenAPI openAPI) {
        if (param.get$ref() == null) {
            return param;
        }
        String ref = param.get$ref();
        String name = ref.substring(ref.lastIndexOf('/') + 1);
        if (openAPI.getComponents() != null && openAPI.getComponents().getParameters() != null) {
            return openAPI.getComponents().getParameters().get(name);
        }
        return null;
    }

    private String extractRequestBodyType(RequestBody requestBody) {
        if (requestBody.getContent() != null
                && requestBody.getContent().containsKey(Constants.CONTENT_TYPE_JSON)) {
            Schema<?> schema = requestBody.getContent().get(Constants.CONTENT_TYPE_JSON).getSchema();
            return schemaToBalType(schema);
        }
        return Constants.BAL_TYPE_JSON;
    }

    private String extractReturnType(Operation operation) {
        if (operation.getResponses() == null) {
            return Constants.BAL_TYPE_JSON;
        }
        for (Map.Entry<String, ApiResponse> entry : operation.getResponses().entrySet()) {
            String statusCode = entry.getKey();
            if (!statusCode.startsWith("2")) {
                continue;
            }
            ApiResponse response = entry.getValue();
            if (response.getContent() == null) {
                return Constants.BAL_TYPE_JSON;
            }
            if (response.getContent().containsKey(Constants.CONTENT_TYPE_JSON)) {
                Schema<?> schema = response.getContent().get(Constants.CONTENT_TYPE_JSON).getSchema();
                return schemaToBalType(schema);
            }
            if (response.getContent().containsKey(Constants.CONTENT_TYPE_TEXT_PLAIN)) {
                return Constants.BAL_TYPE_STRING;
            }
        }
        return Constants.BAL_TYPE_JSON;
    }

    private String buildBalPath(String path, List<ParameterInfo> parameters) {
        Map<String, String> nameToSafe = new LinkedHashMap<>();
        for (ParameterInfo p : parameters) {
            nameToSafe.put(p.getOriginalName(), p.getSafeName());
        }
        String[] segments = path.split("/");
        StringBuilder sb = new StringBuilder();
        for (String segment : segments) {
            if (segment.isEmpty()) continue;
            sb.append("/");
            if (segment.startsWith("{") && segment.endsWith("}")) {
                String paramName = segment.substring(1, segment.length() - 1);
                String safeName = nameToSafe.getOrDefault(paramName, paramName);
                sb.append("${").append(safeName).append("}");
            } else {
                sb.append(segment);
            }
        }
        return sb.toString();
    }

    /**
     * Maps an OpenAPI {@link Schema} to a Ballerina type string.
     *
     * <ul>
     *   <li>{@code $ref} → sanitized PascalCase type name via {@link GeneratorUtils#getValidName}</li>
     *   <li>Arrays → {@code itemType[]}</li>
     *   <li>Primitives → via {@link GeneratorUtils#convertOpenAPITypeToBallerina}</li>
     *   <li>Fallback → {@code json}</li>
     * </ul>
     */
    public static String schemaToBalType(Schema<?> schema) {
        if (schema == null) {
            return Constants.BAL_TYPE_JSON;
        }

        if (schema.get$ref() != null) {
            String ref = schema.get$ref();
            String typeName = ref.substring(ref.lastIndexOf('/') + 1);
            return GeneratorUtils.getValidName(typeName, true);
        }

        String type = effectiveType(schema);

        if (Constants.OAS_TYPE_ARRAY.equals(type)) {
            return schema.getItems() != null
                    ? schemaToBalType(schema.getItems()) + "[]"
                    : Constants.BAL_TYPE_JSON + "[]";
        }

        try {
            return GeneratorUtils.convertOpenAPITypeToBallerina(schema, false);
        } catch (UnsupportedOASDataTypeException e) {
            return Constants.BAL_TYPE_JSON;
        }
    }

    /**
     * Resolves the effective OpenAPI type string from a schema, handling both
     * the OAS 3.0 {@code type} field and the OAS 3.1 {@code types} set.
     */
    private static String effectiveType(Schema<?> schema) {
        String type = schema.getType();
        if (type != null) {
            return type;
        }
        Set<String> types = schema.getTypes();
        if (types == null || types.isEmpty()) {
            return null;
        }
        for (String preferred : List.of(Constants.OAS_TYPE_INTEGER, Constants.OAS_TYPE_NUMBER,
                Constants.OAS_TYPE_BOOLEAN, Constants.BAL_TYPE_STRING, Constants.OAS_TYPE_ARRAY)) {
            if (types.contains(preferred)) {
                return preferred;
            }
        }
        return types.iterator().next();
    }

    /**
     * Builds a camelCase tool/function name from an HTTP method and path when no
     * {@code operationId} is available.
     *
     * <p>e.g. {@code GET /pets/{petId}} → {@code getPetsPetId}
     */
    private static String buildToolName(String method, String path) {
        String[] segments = path.split("/");
        StringBuilder sb = new StringBuilder(method.toLowerCase());
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            String cleaned = segment.replaceAll("[{}]", "");
            if (!cleaned.isEmpty()) {
                sb.append(Character.toUpperCase(cleaned.charAt(0)));
                sb.append(cleaned.substring(1));
            }
        }
        String name = sb.toString().replaceAll("[^a-zA-Z0-9_]", "");
        return name.isEmpty() ? Constants.DEFAULT_TOOL_NAME : GeneratorUtils.escapeIdentifier(name);
    }
}
