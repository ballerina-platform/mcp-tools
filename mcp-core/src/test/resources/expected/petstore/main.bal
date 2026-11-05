import ballerina/log;
import ballerina/mcp.v2 as mcpv2;
import ballerina/http;

final http:Client apiClient = check new ("https://petstore.example.com/v1");
listener mcpv2:StreamableHttpListener mcpListener = check new (9090);

@mcpv2:StreamableHttpServiceConfig {
    info: {
        name: "Petstore",
        version: "1.0.0"
    }
}
service mcpv2:StreamableHttpService /petstore on mcpListener {

    # List all pets
    #
    # + return - The API response, or an error if the request fails
    isolated remote function listPets() returns Pet[]|error {
        log:printInfo("Proxying request to: " + string `/pets`);
        Pet[] response = check apiClient->get(string `/pets`);
        return response;
    }

    # Create a pet
    #
    # + payload -
    # + return - The API response, or an error if the request fails
    isolated remote function createPet(NewPet payload) returns Pet|error {
        log:printInfo("Proxying request to: " + string `/pets`);
        Pet response = check apiClient->post(string `/pets`, payload);
        return response;
    }

    # Info for a specific pet
    #
    # + petId -
    # + return - The API response, or an error if the request fails
    isolated remote function showPetById(int petId) returns Pet|error {
        log:printInfo("Proxying request to: " + string `/pets/${petId}`);
        Pet response = check apiClient->get(string `/pets/${petId}`);
        return response;
    }

    # Delete a pet
    #
    # + petId -
    # + return - The API response, or an error if the request fails
    isolated remote function deletePet(int petId) returns string|error {
        log:printInfo("Proxying request to: " + string `/pets/${petId}`);
        string response = check apiClient->delete(string `/pets/${petId}`);
        return response;
    }

}
