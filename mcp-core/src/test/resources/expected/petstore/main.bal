import ballerina/log;
import ballerina/mcp.v2 as mcpv2;
import ballerina/http;

http:Client apiClient = check new ("https://petstore.example.com/v1");
listener mcpv2:StreamableHttpListener mcpListener = check new (9090);

@mcpv2:StreamableHttpServiceConfig {
    info: {
        name: "Petstore",
        version: "1.0.0"
    }
}
service mcpv2:StreamableHttpService /petstore on mcpListener {

    @mcpv2:Tool {
        description: "List all pets"
    }
    remote function listPets() returns Pet[]|error {
        log:printInfo("Proxying request to: " + string `/pets`);
        Pet[] response = check apiClient->get(string `/pets`);
        return response;
    }

    @mcpv2:Tool {
        description: "Create a pet"
    }
    remote function createPet(NewPet payload) returns Pet|error {
        log:printInfo("Proxying request to: " + string `/pets`);
        Pet response = check apiClient->post(string `/pets`, payload);
        return response;
    }

    @mcpv2:Tool {
        description: "Info for a specific pet"
    }
    remote function showPetById(int petId) returns Pet|error {
        log:printInfo("Proxying request to: " + string `/pets/${petId}`);
        Pet response = check apiClient->get(string `/pets/${petId}`);
        return response;
    }

    @mcpv2:Tool {
        description: "Delete a pet"
    }
    remote function deletePet(int petId) returns string|error {
        log:printInfo("Proxying request to: " + string `/pets/${petId}`);
        string response = check apiClient->delete(string `/pets/${petId}`);
        return response;
    }

}
