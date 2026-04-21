import ballerina/log;
import ballerina/mcp;
import ballerina/http;

http:Client apiClient = check new ("http://localhost:9096");
listener mcp:Listener mcpListener = check new (9096);

@mcp:ServiceConfig {
    info: {
        name: "Ref Query Params API",
        version: "1.0.0"
    }
}
service mcp:Service /ref_query_params_api on mcpListener {

    @mcp:Tool {
        description: "Executes GET on /pets"
    }
    remote function listPetsWithRefParams(int 'limit, int offset) returns json|error {
        log:printInfo("Proxying request to: " + string `/pets?limit=${'limit}&offset=${offset}`);
        json response = check apiClient->get(string `/pets?limit=${'limit}&offset=${offset}`);
        return response;
    }

}
