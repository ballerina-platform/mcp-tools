import ballerina/log;
import ballerina/mcp;
import ballerina/http;

http:Client apiClient = check new ("https://petstore.example.com/v1");
listener mcp:Listener mcpListener = check new (9090);

@mcp:ServiceConfig {
    info: {
        name: "Petstore",
        version: "1.0.0"
    }
}
service mcp:Service /petstore on mcpListener {

    @mcp:Tool {
        description: "List all pets"
    }
    remote function listPets() returns Pet[]|error {
        log:printInfo("Proxying request to: " + string `/pets`);
        Pet[] response = check apiClient->get(string `/pets`);
        return response;
    }

    @mcp:Tool {
        description: "Create a pet"
    }
    remote function createPet(NewPet payload) returns Pet|error {
        log:printInfo("Proxying request to: " + string `/pets`);
        Pet response = check apiClient->post(string `/pets`, payload);
        return response;
    }

    @mcp:Tool {
        description: "Info for a specific pet"
    }
    remote function showPetById(int petId) returns Pet|error {
        log:printInfo("Proxying request to: " + string `/pets/${petId}`);
        Pet response = check apiClient->get(string `/pets/${petId}`);
        return response;
    }

    @mcp:Tool {
        description: "Delete a pet"
    }
    remote function deletePet(int petId) returns string|error {
        log:printInfo("Proxying request to: " + string `/pets/${petId}`);
        string response = check apiClient->delete(string `/pets/${petId}`);
        return response;
    }

}
