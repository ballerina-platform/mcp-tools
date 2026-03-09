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

/**
 * Represents a single parameter of an API endpoint operation.
 */
public final class ParameterInfo {

    private final String originalName;
    private final String safeName;
    private final String balType;
    private final String location;

    /**
     * @param originalName original parameter name from the spec
     * @param safeName     sanitized Ballerina identifier
     * @param balType      Ballerina type string (int, string, boolean, decimal, json)
     * @param location     parameter location: "path", "query", "header", "body"
     */
    public ParameterInfo(String originalName, String safeName, String balType, String location) {
        this.originalName = originalName;
        this.safeName = safeName;
        this.balType = balType;
        this.location = location;
    }

    public String getOriginalName() { return originalName; }
    public String getSafeName() { return safeName; }
    public String getBalType() { return balType; }
    public String getLocation() { return location; }

    /** Returns the function argument declaration: e.g. {@code "int petId"}. */
    public String toArgDeclaration() {
        return balType + " " + safeName;
    }
}
