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
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Unit tests for {@link OpenApiSpecParser} across YAML and JSON fixtures.
 */
public class OpenApiSpecParserTest {

    @DataProvider(name = "yamlJsonSpecs")
    public Object[][] yamlJsonSpecs() {
        return new Object[][]{
                {"specs/petstore.yaml", "specs/petstore.json"},
                {"specs/no-schemas.yaml", "specs/no-schemas.json"},
                {"specs/no-info.yaml", "specs/no-info.json"},
                {"specs/parser-edge-cases.yaml", "specs/parser-edge-cases.json"},
                {"specs/unresolved-server-variable.yaml", "specs/unresolved-server-variable.json"},
                {"specs/no-operation-id.yaml", "specs/no-operation-id.json"},
                {"specs/multi-query-param.yaml", "specs/multi-query-param.json"},
                {"specs/multi-path-param.yaml", "specs/multi-path-param.json"},
                {"specs/ref-query-parameters.yaml", "specs/ref-query-parameters.json"},
                {"specs/multi-method-resources.yaml", "specs/multi-method-resources.json"},
                {"specs/default-value-generation.yaml", "specs/default-value-generation.json"},
                {"specs/empty-paths.yaml", "specs/empty-paths.json"},
                {"specs/path-level-params.yaml", "specs/path-level-params.json"},
                {"specs/blank-server-url.yaml", "specs/blank-server-url.json"}
        };
    }

    @Test(dataProvider = "yamlJsonSpecs")
    public void testParseYamlAndJsonProduceEquivalentSpecInfo(String yamlResource, String jsonResource) throws Exception {
        OpenApiSpecParser parser = new OpenApiSpecParser();

        SpecInfo yamlSpec = parser.parse(resolveResourcePath(yamlResource));
        SpecInfo jsonSpec = parser.parse(resolveResourcePath(jsonResource));

        Assert.assertEquals(jsonSpec.getBaseUrl(), yamlSpec.getBaseUrl(), "Base URL mismatch");
        Assert.assertEquals(jsonSpec.getPort(), yamlSpec.getPort(), "Port mismatch");
        Assert.assertEquals(jsonSpec.getTitle(), yamlSpec.getTitle(), "Title mismatch");
        Assert.assertEquals(jsonSpec.getVersion(), yamlSpec.getVersion(), "Version mismatch");
        Assert.assertEquals(jsonSpec.getEndpoints().size(), yamlSpec.getEndpoints().size(), "Endpoint count mismatch");

        for (int i = 0; i < yamlSpec.getEndpoints().size(); i++) {
            Assert.assertEquals(jsonSpec.getEndpoints().get(i).getToolName(),
                    yamlSpec.getEndpoints().get(i).getToolName(), "Tool name mismatch at endpoint index " + i);
            Assert.assertEquals(jsonSpec.getEndpoints().get(i).getMethod(),
                    yamlSpec.getEndpoints().get(i).getMethod(), "Method mismatch at endpoint index " + i);
            Assert.assertEquals(jsonSpec.getEndpoints().get(i).getPath(),
                    yamlSpec.getEndpoints().get(i).getPath(), "Path mismatch at endpoint index " + i);
        }
    }

    // ── Unsupported file type ──────────────────────────────────────────
    @Test
    public void testParseUnsupportedFileTypeThrowsException() throws Exception {
        Path txtFile = Files.createTempFile("spec", ".txt");
        try {
            new OpenApiSpecParser().parse(txtFile);
            Assert.fail("Expected McpGenerationException");
        } catch (McpGenerationException e) {
            Assert.assertTrue(e.getMessage().contains("Unsupported file type"));
        } finally {
            Files.deleteIfExists(txtFile);
        }
    }

    // ── Invalid/unparseable spec ───────────────────────────────────────
    @Test
    public void testParseInvalidSpecThrowsException() throws Exception {
        Path invalidSpec = Files.createTempFile("invalid", ".yaml");
        Files.writeString(invalidSpec, "this: is: not: valid: openapi");
        try {
            new OpenApiSpecParser().parse(invalidSpec);
            Assert.fail("Expected McpGenerationException");
        } catch (McpGenerationException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to parse OpenAPI spec"));
        } finally {
            Files.deleteIfExists(invalidSpec);
        }
    }

    // ── Server URL blank falls back to default ─────────────────────────
    @Test
    public void testParseSpecWithBlankServerUrlFallsBackToDefault() throws Exception {
        SpecInfo result = new OpenApiSpecParser().parse(resolveResourcePath("specs/blank-server-url.yaml"));
        Assert.assertEquals(result.getBaseUrl(), "http://localhost:8080");
    }

    // ── pathItem.getParameters() not null ─────────────────────────────
    @Test
    public void testParseSpecWithPathLevelParameters() throws Exception {
        SpecInfo result = new OpenApiSpecParser().parse(resolveResourcePath("specs/path-level-params.yaml"));
        Assert.assertFalse(result.getEndpoints().isEmpty());
        Assert.assertFalse(result.getEndpoints().get(0).getParameters().isEmpty());
    }

    // ── Unresolvable $ref in parameters does not cause failure ─────────
    @Test
    public void testParseSpecWithUnresolvableRefParameter() throws Exception {
        SpecInfo result = new OpenApiSpecParser().parse(resolveResourcePath("specs/unresolvable-ref-param.yaml"));
        Assert.assertNotNull(result.getEndpoints());
    }

    // ── schemaToBalType branches ───────────────────────────────────────
    @Test
    public void testSchemaToBalTypeWithNullSchema() {
        Assert.assertEquals(OpenApiSpecParser.schemaToBalType(null), "json");
    }

    @Test
    public void testSchemaToBalTypeWithRefSchema() {
        Schema<?> refSchema = new Schema<>();
        refSchema.set$ref("#/components/schemas/Pet");
        Assert.assertEquals(OpenApiSpecParser.schemaToBalType(refSchema), "Pet");
    }

    @Test
    public void testSchemaToBalTypeWithArraySchema() {
        ArraySchema arraySchema = new ArraySchema();
        arraySchema.setItems(new StringSchema());
        Assert.assertEquals(OpenApiSpecParser.schemaToBalType(arraySchema), "string[]");
    }

    @Test
    public void testSchemaToBalTypeWithArraySchemaNoItems() {
        ArraySchema arraySchema = new ArraySchema();
        Assert.assertEquals(OpenApiSpecParser.schemaToBalType(arraySchema), "json[]");
    }

    // ── effectiveType with OAS 3.1 types set ──────────────────────────
    @Test
    public void testSchemaToBalTypeWithOas31TypesSet() {
        Schema<?> schema = new Schema<>();
        schema.setTypes(new LinkedHashSet<>(List.of("integer")));
        String result = OpenApiSpecParser.schemaToBalType(schema);
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testSchemaToBalTypeWithOas31EmptyTypesSet() {
        Schema<?> schema = new Schema<>();
        schema.setTypes(new LinkedHashSet<>());
        String result = OpenApiSpecParser.schemaToBalType(schema);
        Assert.assertNotNull(result);
    }

    private Path resolveResourcePath(String resourceName) throws Exception {
        URL resource = getClass().getClassLoader().getResource(resourceName);
        Assert.assertNotNull(resource, resourceName + " test resource not found");
        return Paths.get(resource.toURI());
    }
}
