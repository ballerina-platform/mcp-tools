import ballerina/log;
import ballerina/mcp;
import ballerina/http;

http:Client apiClient = check new ("http://localhost:9093");
listener mcp:Listener mcpListener = check new (9093);

@mcp:ServiceConfig {
    info: {
        name: "No Operation Id API",
        version: "1.0.0"
    }
}
service mcp:Service /no_operation_id_api on mcpListener {

    @mcp:Tool {
        description: "List users"
    }
    remote function getUsers() returns json|error {
        log:printInfo("Proxying request to: " + string `/users`);
        json response = check apiClient->get(string `/users`);
        return response;
    }

}
