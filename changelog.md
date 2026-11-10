# Change Log

This file contains all the notable changes done to the Ballerina Email package through the releases.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - Unreleased

### Changed

- Generate MCP servers with the `ballerina/mcp.v2` module (`import ballerina/mcp.v2 as mcpv2;`) and pin `ballerina/mcp` 1.5.0 in the generated `Ballerina.toml`
- Require Ballerina Swan Lake Update 14 (2201.14.0) or later, for both the tool and the generated projects
- Describe each tool with a doc comment instead of the `@mcpv2:Tool` annotation, and document its arguments from the OpenAPI parameter and request body descriptions, so MCP clients see them in the tool's input schema
- Generate `isolated` tool functions and a `final` HTTP client

## [1.1.0] - 2026-09-18

### Added

- [Publish mcp-core as a Maven artifact](https://github.com/ballerina-platform/mcp-tools/pull/11)
- [Generate `mcp:StreamableHttpListener` instead of the deprecated `mcp:Listener`, and generate `mcp:StreamableHttpService` + `@mcp:StreamableHttpServiceConfig`](https://github.com/ballerina-platform/mcp-tools/pull/12)

## [1.0.0] - 2026-04-24

### Added

- [Introduce the CLI tool to generate a Ballerina MCP server project from an OpenAPI/Swagger specification](https://github.com/ballerina-platform/mcp-tools/pull/1)
- [Improve the documentation of the MCP tool](https://github.com/ballerina-platform/mcp-tools/pull/3)
- [Add integration and unit tests](https://github.com/ballerina-platform/mcp-tools/pull/5)
