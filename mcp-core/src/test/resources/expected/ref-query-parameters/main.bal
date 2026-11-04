import ballerina/log;
import ballerina/mcp.v2 as mcpv2;
import ballerina/http;

http:Client apiClient = check new ("http://localhost:9096");
listener mcpv2:StreamableHttpListener mcpListener = check new (9090);

@mcpv2:StreamableHttpServiceConfig {
    info: {
        name: "Ref Query Params API",
        version: "1.0.0"
    }
}
service mcpv2:StreamableHttpService /ref_query_params_api on mcpListener {

    @mcpv2:Tool {
        description: "Executes GET on /pets"
    }
    remote function listPetsWithRefParams(int 'limit, int offset) returns json|error {
        log:printInfo("Proxying request to: " + string `/pets?limit=${'limit}&offset=${offset}`);
        json response = check apiClient->get(string `/pets?limit=${'limit}&offset=${offset}`);
        return response;
    }

}
