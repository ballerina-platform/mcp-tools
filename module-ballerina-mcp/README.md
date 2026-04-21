## Overview

Model Context Protocol (MCP) is a standard way to expose application capabilities as tools that AI assistants can safely discover and invoke.

The Ballerina MCP Tool reads an OpenAPI contract and generates a ready-to-run Ballerina MCP server project.
This helps you quickly expose existing REST APIs as MCP tools with less manual boilerplate.

### Installation

If the tool is not already available in your environment, pull it from Ballerina Central.

```bash
bal tool pull mcp
```

> Note: You can also pull a specific version of the tool.
>
> Example:
>
> ```bash
> bal tool pull mcp:1.0.0
> ```

### Usage

```bash
bal mcp -i <openapi-contract> [-o <output-dir>] [--contract-type openapi]
```

#### Command Options

| Option            | Description                                                 | Mandatory/Optional |
| ----------------- | ----------------------------------------------------------- | ------------------ |
| `-i`, `--input`   | Path to the OpenAPI/Swagger YAML or JSON contract file      | Mandatory          |
| `-o`, `--output`  | Directory where the generated MCP project will be created   | Optional           |
| `--contract-type` | Type of API contract to process (currently only: `openapi`) | Optional           |
| `-h`, `--help`    | Show command usage and available options                    | Optional           |

### Generate an MCP Server Project

```bash
bal mcp -i ./petstore.yaml -o ./petstore-mcp
```

> Note: The generated project folder name is normalized at creation time to a valid Ballerina package name (lowercased, with hyphens and other non-alphanumeric characters converted to underscores). For example, a name like `petstore-mcp` becomes `petstore_mcp`. The `-o` value is the parent output path, so the created folder name may differ from the `-o` argument.

If generation is successful, the output directory will contain a project similar to:

```text
petstore_mcp/
├── Ballerina.toml
├── main.bal
├── types.bal
└── README.md
```

> Note: The tool currently supports only the `openapi` contract type.
