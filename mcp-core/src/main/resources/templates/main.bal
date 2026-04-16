import ballerina/log;
import ballerina/mcp;
import ballerina/http;

http:Client apiClient = check new ("{{BASE_URL}}");
listener mcp:Listener mcpListener = check new ({{PORT}});

@mcp:ServiceConfig {
    info: {
        name: "{{TITLE}}",
        version: "{{VERSION}}"
    }
}
service mcp:Service /{{SERVICE_PATH}} on mcpListener {
{{REMOTE_FUNCTIONS}}
}
