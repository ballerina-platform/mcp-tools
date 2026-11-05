import ballerina/log;
import ballerina/mcp.v2 as mcpv2;
import ballerina/http;

final http:Client apiClient = check new ("http://localhost:9094");
listener mcpv2:StreamableHttpListener mcpListener = check new (9090);

@mcpv2:StreamableHttpServiceConfig {
    info: {
        name: "Descriptions API",
        version: "1.0.0"
    }
}
service mcpv2:StreamableHttpService /descriptions_api on mcpListener {

    # Get an item
    #
    # + itemId - ID of the item, matching ^\d+$
    # + 'limit - Maximum number of entries to return
    # + verbose -
    # + return - The API response, or an error if the request fails
    isolated remote function getItem(string itemId, int 'limit, boolean verbose) returns json|error {
        log:printInfo("Proxying request to: " + string `/items/${itemId}?limit=${'limit}&verbose=${verbose}`);
        json response = check apiClient->get(string `/items/${itemId}?limit=${'limit}&verbose=${verbose}`);
        return response;
    }

    # Creates an item.
    #
    # Rules:
    # - names must be unique
    #  +1 for short names
    # Names match ^[a-z]\w*$, and "C:\temp" style paths are kept as written.
    #
    # + payload - The item to create
    # + return - The API response, or an error if the request fails
    isolated remote function createItem(record {} payload) returns record {}|error {
        log:printInfo("Proxying request to: " + string `/items`);
        record {} response = check apiClient->post(string `/items`, payload);
        return response;
    }

}
