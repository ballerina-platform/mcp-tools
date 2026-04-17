# Ballerina MCP Tool

Generate a Ballerina MCP (Model Context Protocol) server project from an OpenAPI or Swagger contract.

## Package Overview

OpenAPI is a standard way to describe HTTP APIs, including endpoints, parameters, request bodies, and responses.

The Ballerina MCP Tool reads an OpenAPI contract and generates a ready-to-run Ballerina MCP server project.
This helps you quickly expose existing REST APIs as MCP tools with less manual boilerplate.

### Installation

If the tool is not already available in your environment, pull it from Ballerina Central.

```bash
bal tool pull mcp:0.1.0
```

### Usage

```bash
bal mcp -i <openapi-contract> [-o <output-dir>] [--contract-type openapi]
```

#### Command Options

| Option            | Description                                               | Mandatory/Optional |
| ----------------- | --------------------------------------------------------- | ------------------ |
| `-i`, `--input`   | Path to the OpenAPI/Swagger YAML or JSON contract file    | Mandatory          |
| `-o`, `--output`  | Directory where the generated MCP project will be created | Optional           |
| `--contract-type` | Contract format (currently: `openapi`)                    | Optional           |
| `-h`, `--help`    | Print command help                                        | Optional           |

### Generate an MCP Server Project

```bash
bal mcp -i ./petstore.yaml -o ./petstore-mcp
```

If generation is successful, the output directory will contain a project similar to:

```text
petstore_mcp/
├── Ballerina.toml
├── main.bal
├── types.bal
└── README.md
```

### Notes

- The tool currently supports the `openapi` contract type.
