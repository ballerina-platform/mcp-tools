import ballerina/log;
import ballerina/mcp.v2 as mcpv2;
import ballerina/http;

http:Client apiClient = check new ("http://localhost:9094");
listener mcpv2:StreamableHttpListener mcpListener = check new (9090);

@mcpv2:StreamableHttpServiceConfig {
    info: {
        name: "Multi Query API",
        version: "1.0.0"
    }
}
service mcpv2:StreamableHttpService /multi_query_api on mcpListener {

    @mcpv2:Tool {
        description: "Executes GET on /search"
    }
    remote function searchItems(string q, int page, int pageSize) returns json|error {
        log:printInfo("Proxying request to: " + string `/search?q=${q}&page=${page}&page_size=${pageSize}`);
        json response = check apiClient->get(string `/search?q=${q}&page=${page}&page_size=${pageSize}`);
        return response;
    }

}
