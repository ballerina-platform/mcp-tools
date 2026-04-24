# Ballerina MCP Tools

[![Build](https://github.com/ballerina-platform/mcp-tools/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/ballerina-platform/mcp-tools/actions/workflows/build-timestamped-master.yml)
[![codecov](https://codecov.io/gh/ballerina-platform/mcp-tools/branch/main/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/mcp-tools)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/mcp-tools.svg)](https://github.com/ballerina-platform/mcp-tools/commits/main)
[![GitHub issues](https://img.shields.io/github/issues/ballerina-platform/mcp-tools.svg?label=Open%20Issues)](https://github.com/wso2/product-integrator/issues?q=is%3Aissue%20state%3Aopen%20label%3Amcp-tools)

A Ballerina CLI tool that generates a Ballerina MCP (Model Context Protocol) server project
from an OpenAPI/Swagger specification.

## Command

```bash
bal mcp -i <openapi-contract> -o <output-dir> --contract-type openapi
```

### Options

| Flag              | Description                                       | Default           |
| ----------------- | ------------------------------------------------- | ----------------- |
| `-i`, `--input`   | Path to the OpenAPI/Swagger YAML or JSON contract | _(required)_      |
| `-o`, `--output`  | Output directory for the generated project        | `.` (current dir) |
| `--contract-type` | Contract format: `openapi`                        | `openapi`         |

### Example

```bash
bal mcp -i ./petstore.yaml -o ./output --contract-type openapi
```

### Generated Project Structure

```text
petstore_mcp/
├── Ballerina.toml    # Project configuration
├── main.bal          # MCP service with remote functions per API endpoint
├── types.bal         # Ballerina record types from API schemas
└── README.md         # Project documentation
```

## Modules

| Module                 | Description                                    |
| ---------------------- | ---------------------------------------------- |
| `mcp-core`             | Core parsing and code generation logic         |
| `mcp-cli`              | Ballerina CLI integration (`bal mcp` command)  |
| `module-ballerina-mcp` | Distributable Ballerina tool package (`.bala`) |

## Building

```bash
./gradlew build
```

## Running Tests

```bash
./gradlew test
```
