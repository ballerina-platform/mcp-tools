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

import io.ballerina.mcp.core.model.SchemaInfo;
import io.ballerina.mcp.core.model.SpecInfo;

import java.util.Map;

/**
 * Generates the content of {@code types.bal} for a Ballerina MCP server project.
 *
 * <p>Each OpenAPI schema component is rendered as a closed Ballerina record ({@code record {| ... |}}).
 * Fields not present in the schema's {@code required} list are rendered as optional ({@code ?}).
 * Names that required sanitization get a {@code @jsondata:Name} annotation.
 */
public class TypesBalGenerator {

    private static final String NL = System.lineSeparator();
    private static final String INDENT = "    ";

    /**
     * Generates the full content of {@code types.bal}.
     *
     * @param spec the parsed spec information
     * @return the generated Ballerina source as a string, or {@code null} if there are no schemas
     */
    public String generate(SpecInfo spec) {
        Map<String, SchemaInfo> schemas = spec.getSchemas();
        if (schemas == null || schemas.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("import ballerina/data.jsondata;").append(NL);
        sb.append(NL);

        for (SchemaInfo schema : schemas.values()) {
            appendRecord(sb, schema);
        }

        return sb.toString();
    }

    // -----------------------------------------------------------------------

    private void appendRecord(StringBuilder sb, SchemaInfo schema) {
        sb.append("type ").append(schema.getBalName()).append(" record {|").append(NL);

        for (SchemaInfo.FieldInfo field : schema.getFields()) {
            // Field-level @jsondata:Name annotation if identifier was sanitized
            if (field.isNeedsAnnotation()) {
                sb.append(INDENT)
                        .append("@jsondata:Name {value: \"").append(field.getOriginalName()).append("\"}")
                        .append(NL);
            }

            // Field declaration: type fieldName[?];
            sb.append(INDENT).append(field.getBalType()).append(" ").append(field.getFieldIdent());
            if (!field.isRequired()) {
                sb.append("?");
            }
            sb.append(";").append(NL);
        }

        sb.append("|};").append(NL);
        sb.append(NL);
    }
}
