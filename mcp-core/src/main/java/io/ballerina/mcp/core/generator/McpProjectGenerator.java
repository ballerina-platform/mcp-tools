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

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.mcp.core.model.SpecInfo;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.nio.file.Path;

/**
 * Orchestrates the full MCP project generation pipeline.
 *
 * <ol>
 *   <li>Parse the input OpenAPI spec → {@link SpecInfo} + raw {@link OpenAPI}</li>
 *   <li>Derive package name and output directory</li>
 *   <li>Generate and write {@code main.bal}</li>
 *   <li>Generate and write {@code types.bal} via {@link TypeHandler} (if schemas exist)</li>
 *   <li>Generate and write {@code Ballerina.toml}</li>
 *   <li>Generate and write {@code README.md}</li>
 * </ol>
 */
public class McpProjectGenerator {

    private static final String FILE_MAIN_BAL = "main.bal";
    private static final String FILE_TYPES_BAL = "types.bal";
    private static final String FILE_BALLERINA_TOML = "Ballerina.toml";
    private static final String FILE_README_MD = "README.md";

    private final GeneratorOptions options;

    private final OpenApiSpecParser specParser = new OpenApiSpecParser();
    private final MainBalGenerator mainBalGenerator = new MainBalGenerator();
    private final BallerinaTomlGenerator tomlGenerator = new BallerinaTomlGenerator();
    private final ReadmeGenerator readmeGenerator = new ReadmeGenerator();

    public McpProjectGenerator(GeneratorOptions options) {
        this.options = options;
    }

    /**
     * Runs the full generation pipeline.
     *
     * @throws McpGenerationException if parsing or file I/O fails
     */
    public void generate() throws McpGenerationException {
        SpecInfo spec = specParser.parse(options.getInputPath());
        OpenAPI openAPI = specParser.getOpenAPI();

        String packageName = GeneratorUtils.derivePackageName(spec.getTitle());
        Path projectDir = options.getOutputPath().resolve(packageName);

        GeneratorUtils.createDirectory(projectDir);

        String mainBal = mainBalGenerator.generate(spec);
        GeneratorUtils.writeFile(projectDir.resolve(FILE_MAIN_BAL), mainBal);

        String typesBal = generateTypesBal(openAPI);
        if (typesBal != null) {
            GeneratorUtils.writeFile(projectDir.resolve(FILE_TYPES_BAL), typesBal);
        }

        String toml = tomlGenerator.generate(spec, packageName);
        GeneratorUtils.writeFile(projectDir.resolve(FILE_BALLERINA_TOML), toml);

        String readme = readmeGenerator.generate(spec);
        GeneratorUtils.writeFile(projectDir.resolve(FILE_README_MD), readme);

        System.out.println("Generated files:");
        System.out.println("  " + projectDir.resolve(FILE_MAIN_BAL));
        if (typesBal != null) {
            System.out.println("  " + projectDir.resolve(FILE_TYPES_BAL));
        }
        System.out.println("  " + projectDir.resolve(FILE_BALLERINA_TOML));
        System.out.println("  " + projectDir.resolve(FILE_README_MD));
    }

    /**
     * Generates {@code types.bal} content using {@link TypeHandler} from
     * {@code io.ballerina.openapi:core}.
     *
     * <p>For each schema in {@code components/schemas}, a {@code $ref} schema is fed into
     * {@link TypeHandler#getTypeNodeFromOASSchema} which resolves the reference, generates the
     * {@code TypeDefinitionNode}, and registers it (along with any nested sub-types) in the
     * handler's internal registry. {@link TypeHandler#generateTypeSyntaxTree()} then emits
     * the complete, formatter-ready {@link SyntaxTree}.
     *
     * @return the types.bal source string, or {@code null} if there are no schemas
     * @throws McpGenerationException if type generation fails
     */
    private String generateTypesBal(OpenAPI openAPI) throws McpGenerationException {
        if (openAPI.getComponents() == null
                || openAPI.getComponents().getSchemas() == null
                || openAPI.getComponents().getSchemas().isEmpty()) {
            return null;
        }

        try {
            TypeHandler.createInstance(openAPI, false);

            for (String schemaName : openAPI.getComponents().getSchemas().keySet()) {
                Schema<?> refSchema = new Schema<>();
                refSchema.set$ref("#/components/schemas/" + schemaName);
                TypeHandler.getInstance().getTypeNodeFromOASSchema(refSchema, false);
            }

            SyntaxTree syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
            return Formatter.format(syntaxTree).toSourceCode();
        } catch (FormatterException e) {
            throw new McpGenerationException("Failed to format " + FILE_TYPES_BAL + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new McpGenerationException("Failed to generate " + FILE_TYPES_BAL + ": " + e.getMessage(), e);
        }
    }
}
