import ballerina/log;
import ballerina/mcp.v2 as mcpv2;
import ballerina/http;

final http:Client apiClient = check new ("{{BASE_URL}}");
listener mcpv2:StreamableHttpListener mcpListener = check new ({{PORT}});

@mcpv2:StreamableHttpServiceConfig {
    info: {
        name: "{{TITLE}}",
        version: "{{VERSION}}"
    }
}
service mcpv2:StreamableHttpService /{{SERVICE_PATH}} on mcpListener {
{{REMOTE_FUNCTIONS}}
}
