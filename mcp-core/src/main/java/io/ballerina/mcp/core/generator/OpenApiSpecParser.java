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
import io.ballerina.mcp.core.model.EndpointInfo;
import io.ballerina.mcp.core.model.ParameterInfo;
import io.ballerina.mcp.core.model.SchemaInfo;
import io.ballerina.mcp.core.model.SpecInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses an OpenAPI 3.x or Swagger 2.0 specification and returns a {@link SpecInfo}
 * containing all information needed for Ballerina MCP code generation.
 *
 * <p>Mirrors the logic of the reference shell script:
 * <ul>
 *   <li>Base URL extraction (OAS3 servers[] with variable resolution; Swagger 2.0 schemes+host+basePath)</li>
 *   <li>Path → HTTP method → operation enumeration</li>
 *   <li>Path/body parameter extraction with type mapping</li>
 *   <li>2xx response return-type extraction</li>
 *   <li>Schema (components.schemas / definitions) → record type extraction</li>
 * </ul>
 */
public class OpenApiSpecParser {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    /**
     * Parses the spec file at the given path and returns a {@link SpecInfo}.
     *
     * @param specPath path to the OpenAPI/Swagger YAML or JSON file
     * @return parsed spec information
     * @throws McpGenerationException if the file cannot be parsed
     */
    public SpecInfo parse(Path specPath) throws McpGenerationException {
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

        String baseUrl = extractBaseUrl(openAPI);
        String title = openAPI.getInfo() != null ? openAPI.getInfo().getTitle() : "Proxy Service";
        String version = openAPI.getInfo() != null ? openAPI.getInfo().getVersion() : "1.0.0";

        List<EndpointInfo> endpoints = extractEndpoints(openAPI);
        Map<String, SchemaInfo> schemas = extractSchemas(openAPI);

        return new SpecInfo(baseUrl, title, version, endpoints, schemas);
    }

    // -----------------------------------------------------------------------
    // Base URL extraction
    // -----------------------------------------------------------------------

    private String extractBaseUrl(OpenAPI openAPI) {
        if (openAPI.getServers() != null && !openAPI.getServers().isEmpty()) {
            Server server = openAPI.getServers().get(0);
            String url = server.getUrl();
            if (url == null || url.isBlank()) {
                return DEFAULT_BASE_URL;
            }
            // Resolve server variables with their default values
            if (server.getVariables() != null) {
                for (Map.Entry<String, ServerVariable> entry : server.getVariables().entrySet()) {
                    String placeholder = "{" + entry.getKey() + "}";
                    String defaultValue = entry.getValue().getDefault();
                    if (defaultValue != null) {
                        url = url.replace(placeholder, defaultValue);
                    }
                }
            }
            return url;
        }
        return DEFAULT_BASE_URL;
    }

    // -----------------------------------------------------------------------
    // Endpoint extraction
    // -----------------------------------------------------------------------

    private List<EndpointInfo> extractEndpoints(OpenAPI openAPI) {
        List<EndpointInfo> endpoints = new ArrayList<>();
        if (openAPI.getPaths() == null) {
            return endpoints;
        }

        for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
            String path = pathEntry.getKey();
            PathItem pathItem = pathEntry.getValue();

            addEndpointIfPresent(endpoints, path, "get",    pathItem.getGet(),    pathItem, openAPI);
            addEndpointIfPresent(endpoints, path, "post",   pathItem.getPost(),   pathItem, openAPI);
            addEndpointIfPresent(endpoints, path, "put",    pathItem.getPut(),    pathItem, openAPI);
            addEndpointIfPresent(endpoints, path, "delete", pathItem.getDelete(), pathItem, openAPI);
            addEndpointIfPresent(endpoints, path, "patch",  pathItem.getPatch(),  pathItem, openAPI);
        }
        return endpoints;
    }

    

    private void addEndpointIfPresent(List<EndpointInfo> endpoints,
                                       String path, String method, Operation operation, PathItem pathItem, OpenAPI openAPI) {
        if (operation == null) {
            return;
        }

        // Tool / function name
        String toolName;
        if (operation.getOperationId() != null && !operation.getOperationId().isBlank()) {
            toolName = sanitizeOperationId(operation.getOperationId(), method, path);
        } else {
            toolName = NameSanitizer.buildToolName(method, path);
        }

        // Description
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
        for (Parameter param : mergedParams.values()) {
            if ("path".equals(param.getIn())) {
                String balType = schemaToBalType(param.getSchema());
                String safeName = NameSanitizer.safeIdent(param.getName());
                parameters.add(new ParameterInfo(param.getName(), safeName, balType, "path"));
            }
        }

        // Request body type
        String bodyType = null;
        if (operation.getRequestBody() != null) {
            bodyType = extractRequestBodyType(operation.getRequestBody());
        }

        String returnType = extractReturnType(operation);

        String balPath = buildBalPath(path, parameters);

        endpoints.add(new EndpointInfo(
                path, balPath, method, toolName, description,
                parameters, bodyType, returnType));
    }

    private String sanitizeOperationId(String operationId, String method, String path) {
        String[] parts = operationId.split("[^a-zA-Z0-9]+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            if (i == 0) {
                sb.append(parts[i]);
            } else {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
                sb.append(parts[i].substring(1));
            }
        }
        String result = sb.toString();
        return result.isEmpty() ? NameSanitizer.buildToolName(method, path) : result;
    }

    private Parameter resolveParameter(Parameter param, OpenAPI openAPI) {
            if (param.get$ref() == null) {
                return param;
            }
            String ref = param.get$ref();
            String name = ref.substring(ref.lastIndexOf('/') + 1);
            if (openAPI.getComponents() != null
                    && openAPI.getComponents().getParameters() != null) {
                return openAPI.getComponents().getParameters().get(name);
            }
            return null;
    }

    private String extractRequestBodyType(RequestBody requestBody) {
        if (requestBody.getContent() != null
                && requestBody.getContent().containsKey("application/json")) {
            Schema<?> schema = requestBody.getContent().get("application/json").getSchema();
            return schemaToBalType(schema);
        }
        return "json";
    }

    private String extractReturnType(Operation operation) {
        if (operation.getResponses() == null) {
            return "json";
        }
        for (Map.Entry<String, ApiResponse> entry : operation.getResponses().entrySet()) {
            String statusCode = entry.getKey();
            if (!statusCode.startsWith("2")) {
                continue;
            }
            ApiResponse response = entry.getValue();
            if (response.getContent() == null) {
                return "json";
            }
            if (response.getContent().containsKey("application/json")) {
                Schema<?> schema = response.getContent().get("application/json").getSchema();
                return schemaToBalType(schema);
            }
            if (response.getContent().containsKey("text/plain")) {
                return "string";
            }
        }
        return "json";
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

    // -----------------------------------------------------------------------
    // Schema extraction
    // -----------------------------------------------------------------------

    private Map<String, SchemaInfo> extractSchemas(OpenAPI openAPI) {
        Map<String, SchemaInfo> result = new LinkedHashMap<>();

        Map<String, Schema> schemas = null;
        if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            schemas = openAPI.getComponents().getSchemas();
        }
        if (schemas == null) {
            return result;
        }

        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            String origName = entry.getKey();
            String balName = NameSanitizer.sanitizeTypeName(origName);
            Schema<?> schema = entry.getValue();

            List<SchemaInfo.FieldInfo> fields = new ArrayList<>();
            if (schema.getProperties() != null) {
                for (Map.Entry<?, ?> propEntry : schema.getProperties().entrySet()) {
                    String propName = (String) propEntry.getKey();
                    Schema<?> propSchema = (Schema<?>) propEntry.getValue();
                    String balType = schemaToBalType(propSchema);
                    String fieldIdent = NameSanitizer.safeIdent(propName);
                    boolean required = schema.getRequired() != null
                            && schema.getRequired().contains(propName);
                    boolean needsAnnotation = !fieldIdent.equals(propName);
                    fields.add(new SchemaInfo.FieldInfo(propName, fieldIdent, balType, required, needsAnnotation));
                }
            }

            boolean needsTypeAnnotation = !balName.equals(origName);
            result.put(origName, new SchemaInfo(origName, balName, fields, needsTypeAnnotation));
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Type mapping (mirrors the script's inline jq type resolution)
    // -----------------------------------------------------------------------

    /**
     * Maps an OpenAPI {@link Schema} to a Ballerina type string.
     * Handles $ref, primitives (integer, string, boolean, number), and arrays.
     * Falls back to {@code json} for anything else.
     *
     * @param schema the OpenAPI schema to map
     * @return Ballerina type name
     */
    public static String schemaToBalType(Schema<?> schema) {
        if (schema == null) {
            return "json";
        }
        // $ref
        if (schema.get$ref() != null) {
            String ref = schema.get$ref();
            String typeName = ref.substring(ref.lastIndexOf('/') + 1);
            return NameSanitizer.sanitizeTypeName(typeName);
        }

        String type = schema.getType();
        if (type == null && schema.getTypes() != null && !schema.getTypes().isEmpty()) {
            type = schema.getTypes().iterator().next();
        }

        // Array type
        if ("array".equals(type)) {
            if (schema.getItems() != null) {
                return schemaToBalType(schema.getItems()) + "[]";
            }
            return "json[]";
        }

        return switch (type == null ? "" : type) {
            case "integer" -> "int";
            case "string" -> "string";
            case "boolean" -> "boolean";
            case "number" -> "decimal";
            default -> "json";
        };
    }
}
