import ballerina/log;
import ballerina/mcp;
import ballerina/http;

http:Client apiClient = check new ("{{BASE_URL}}");
listener mcp:StreamableHttpListener mcpListener = check new ({{PORT}});

@mcp:StreamableHttpServiceConfig {
    info: {
        name: "{{TITLE}}",
        version: "{{VERSION}}"
    }
}
service mcp:StreamableHttpService /{{SERVICE_PATH}} on mcpListener {
{{REMOTE_FUNCTIONS}}
}
