import ballerina/log;
import ballerina/mcp;
import ballerina/http;

http:Client apiClient = check new ("http://localhost:9094");
listener mcp:Listener mcpListener = check new (9094);

@mcp:ServiceConfig {
    info: {
        name: "Multi Query API",
        version: "1.0.0"
    }
}
service mcp:Service /multi_query_api on mcpListener {

    @mcp:Tool {
        description: "Executes GET on /search"
    }
    remote function searchItems(string q, int page, int pageSize) returns json|error {
        log:printInfo("Proxying request to: " + string `/search?q=${q}&page=${page}&page_size=${pageSize}`);
        json response = check apiClient->get(string `/search?q=${q}&page=${page}&page_size=${pageSize}`);
        return response;
    }

}
