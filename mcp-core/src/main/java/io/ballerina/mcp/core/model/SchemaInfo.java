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
 * Represents a single OpenAPI schema that maps to a Ballerina {@code record} type.
 */
public final class SchemaInfo {

    private final String originalName;
    private final String balName;
    private final List<FieldInfo> fields;
    private final boolean needsTypeAnnotation;

    public SchemaInfo(String originalName, String balName,
                      List<FieldInfo> fields, boolean needsTypeAnnotation) {
        this.originalName = originalName;
        this.balName = balName;
        this.fields = fields;
        this.needsTypeAnnotation = needsTypeAnnotation;
    }

    /** Original name as it appears in the spec's components/schemas. */
    public String getOriginalName() { return originalName; }

    /** Sanitized Ballerina type name. */
    public String getBalName() { return balName; }

    /** List of record field definitions. */
    public List<FieldInfo> getFields() { return fields; }

    /**
     * Whether a {@code @jsondata:Name} annotation is needed on the type declaration
     * because the Ballerina name differs from the original schema name.
     */
    public boolean isNeedsTypeAnnotation() { return needsTypeAnnotation; }

    // -----------------------------------------------------------------------
    // Nested FieldInfo
    // -----------------------------------------------------------------------

    /**
     * Represents a single field in a Ballerina record type.
     */
    public static final class FieldInfo {

        private final String originalName;
        private final String fieldIdent;
        private final String balType;
        private final boolean required;
        private final boolean needsAnnotation;

        public FieldInfo(String originalName, String fieldIdent, String balType,
                         boolean required, boolean needsAnnotation) {
            this.originalName = originalName;
            this.fieldIdent = fieldIdent;
            this.balType = balType;
            this.required = required;
            this.needsAnnotation = needsAnnotation;
        }

        /** Original property name from the spec. */
        public String getOriginalName() { return originalName; }

        /** Sanitized Ballerina field identifier. */
        public String getFieldIdent() { return fieldIdent; }

        /** Ballerina type string. */
        public String getBalType() { return balType; }

        /** Whether this field is in the schema's {@code required} list. */
        public boolean isRequired() { return required; }

        /**
         * Whether a {@code @jsondata:Name} annotation is needed on this field
         * because its Ballerina identifier differs from the original property name.
         */
        public boolean isNeedsAnnotation() { return needsAnnotation; }
    }
}
