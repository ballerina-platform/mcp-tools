import ballerina/log;
import ballerina/mcp.v2 as mcpv2;
import ballerina/http;

final http:Client apiClient = check new ("http://localhost:9093");
listener mcpv2:StreamableHttpListener mcpListener = check new (9090);

@mcpv2:StreamableHttpServiceConfig {
    info: {
        name: "No Operation Id API",
        version: "1.0.0"
    }
}
service mcpv2:StreamableHttpService /no_operation_id_api on mcpListener {

    # List users
    #
    # + return - The API response, or an error if the request fails
    isolated remote function getUsers() returns json|error {
        log:printInfo("Proxying request to: " + string `/users`);
        json response = check apiClient->get(string `/users`);
        return response;
    }

}
