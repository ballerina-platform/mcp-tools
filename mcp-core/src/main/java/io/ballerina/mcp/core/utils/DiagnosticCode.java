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

package io.ballerina.mcp.core.utils;

/**
 * Diagnostic codes for the Ballerina MCP tool.
 */
public enum DiagnosticCode {
    UNSUPPORTED_INPUT_TYPE("MCP_ERROR_001", "unsupported.input.type"),
    INPUT_FILE_NOT_FOUND("MCP_ERROR_002", "input.file.not.found"),
    MCP_GENERATION_FAILED("MCP_ERROR_003", "mcp.generation.failed"),
    UNRESOLVED_SERVER_VARIABLE("MCP_WARNING_001", "unresolved.server.variable");

    private final String diagnosticId;
    private final String messageKey;

    DiagnosticCode(String diagnosticId, String messageKey) {
        this.diagnosticId = diagnosticId;
        this.messageKey = messageKey;
    }

    public String diagnosticId() {
        return diagnosticId;
    }

    public String messageKey() {
        return messageKey;
    }
}
