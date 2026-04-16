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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Loads template files from the {@code templates/} classpath resource directory.
 */
class TemplateLoader {

    private static final String TEMPLATES_DIR = "templates/";

    private TemplateLoader() {
    }

    static String load(String templateName) throws IOException {
        String resourcePath = TEMPLATES_DIR + templateName;
        try (InputStream in = TemplateLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Template not found: " + resourcePath);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
    }
}
