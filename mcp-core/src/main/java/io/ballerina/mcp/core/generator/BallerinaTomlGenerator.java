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

import io.ballerina.mcp.core.model.SpecInfo;
import org.wso2.ballerinalang.util.RepoUtils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the content of {@code Ballerina.toml} for a Ballerina MCP server project.
 */
public class BallerinaTomlGenerator {

    private static final String NL = System.lineSeparator();

    /**
     * Generates the full content of {@code Ballerina.toml}.
     *
     * @param spec        the parsed spec information
     * @param packageName the Ballerina package name (derived from title)
     * @return the generated TOML content as a string
     */
    public String generate(SpecInfo spec, String packageName) {
        String version = normalizeVersion(spec.getVersion());

        StringBuilder sb = new StringBuilder();
        sb.append("[package]").append(NL);
        sb.append("org = \"").append(getOrgName()).append("\"").append(NL);
        sb.append("name = \"").append(packageName).append("\"").append(NL);
        sb.append("version = \"").append(version).append("\"").append(NL);
        sb.append("distribution = \"").append(RepoUtils.getBallerinaShortVersion()).append("\"").append(NL);
        sb.append(NL);
        sb.append("[build-options]").append(NL);
        sb.append("observabilityIncluded = true").append(NL);

        return sb.toString();
    }

    private String getOrgName() {
        String userName = System.getProperty("user.name");
        if (userName == null) {
            return "my_org";
        }
        return userName.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase(Locale.getDefault());
    }

    /**
     * Normalizes the version string to a valid SemVer format (major.minor.patch).
     */
    private String normalizeVersion(String version) {
        if (version == null || version.isBlank()) {
            return "0.1.0";
        }
        String v = version.startsWith("v") ? version.substring(1) : version;
        Matcher matcher = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?").matcher(v);
        if (!matcher.find()) {
            return "0.1.0";
        }
        String major = matcher.group(1);
        String minor = matcher.group(2) != null ? matcher.group(2) : "0";
        String patch = matcher.group(3) != null ? matcher.group(3) : "0";
        return major + "." + minor + "." + patch;
    }
}
