import ballerina/log;
import ballerina/mcp.v2 as mcpv2;
import ballerina/http;

http:Client apiClient = check new ("http://localhost:9091");
listener mcpv2:StreamableHttpListener mcpListener = check new (9091);

@mcpv2:StreamableHttpServiceConfig {
    info: {
        name: "Minimal API",
        version: "1.0.0"
    }
}
service mcpv2:StreamableHttpService /minimal_api on mcpListener {

    @mcpv2:Tool {
        description: "Health check"
    }
    remote function getHealth() returns record {}|error {
        log:printInfo("Proxying request to: " + string `/health`);
        record {} response = check apiClient->get(string `/health`);
        return response;
    }

}
