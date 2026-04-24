import ballerina/log;
import ballerina/mcp;
import ballerina/http;

http:Client apiClient = check new ("http://localhost:9091");
listener mcp:Listener mcpListener = check new (9091);

@mcp:ServiceConfig {
    info: {
        name: "Minimal API",
        version: "1.0.0"
    }
}
service mcp:Service /minimal_api on mcpListener {

    @mcp:Tool {
        description: "Health check"
    }
    remote function getHealth() returns record {}|error {
        log:printInfo("Proxying request to: " + string `/health`);
        record {} response = check apiClient->get(string `/health`);
        return response;
    }

}
