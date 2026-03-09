# Ballerina MCP Tools

A Ballerina CLI tool that generates a Ballerina MCP (Model Context Protocol) server project
from an OpenAPI/Swagger specification.

## Command

```bash
bal mcp -i <openapi-contract> -o <output-dir> --input-type openapi
```

### Options

| Flag             | Description                                       | Default           |
| ---------------- | ------------------------------------------------- | ----------------- |
| `-i`, `--input`  | Path to the OpenAPI/Swagger YAML or JSON contract | _(required)_      |
| `-o`, `--output` | Output directory for the generated project        | `.` (current dir) |
| `--input-type`   | Contract format: `openapi`                        | `openapi`         |

### Example

```bash
bal mcp -i ./petstore.yaml -o ./output --input-type openapi
```

### Generated Project Structure

```
petstore_mcp/
├── Ballerina.toml    # Project configuration
├── main.bal          # MCP service with remote functions per API endpoint
├── types.bal         # Ballerina record types from API schemas
└── README.md         # Project documentation
```

## Modules

| Module     | Description                                   |
| ---------- | --------------------------------------------- |
| `mcp-core` | Core parsing and code generation logic        |
| `mcp-cli`  | Ballerina CLI integration (`bal mcp` command) |

## Building

```bash
./gradlew build
```

## Running Tests

```bash
./gradlew test
```
