package Petstore.api.automation;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.*;
import java.io.FileReader;
import java.io.FileWriter; // Import FileWriter

import org.hamcrest.Matchers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class PetstoreRequests {
	
//	@Test(priority=1)
//	void addaNewPet() {
//	    given()
//	        .contentType("application/json")
//	        .body("{ \"id\": 2, \"category\": { \"id\": 5, \"name\": \"Ghost\" }, \"name\": \"Ghost\", \"photoUrls\": [ \"abcdefg.lk\" ], \"tags\": [ { \"id\": 2, \"name\": \"Ghost\" } ], \"status\": \"pending\" }")
//	    .when()
//	        .post("https://petstore.swagger.io/v2/pet")
//	    .then()
//	        .statusCode(200);
//	}
	
	@Test(priority=1)
	void addANewPet() {
	    RequestSpecification request = RestAssured.given();
	    request.contentType("application/json");
	    request.body("{ \"id\": 7, \"category\": { \"id\": 5, \"name\": \"Bravo\" }, \"name\": \"Bravo\", \"photoUrls\": [ \"abcdefg.lk\" ], \"tags\": [ { \"id\": 7, \"name\": \"Bravo\" } ], \"status\": \"pending\" }");

	    Response response = request.post("https://petstore.swagger.io/v2/pet");
	    Assert.assertEquals(response.getStatusCode(), 200);
	}


    @Test(priority=2)
    void createPetsAndStoreIDs() {
        JSONParser parser = new JSONParser();
        try {
            // Read data from the JSON file
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader("src/test/resources/pet-store.json"));

            // Create a JSONObject to store pet IDs
            JSONObject petIds = new JSONObject();

            // Iterate over each pet data and create pets
            for (Object obj : jsonArray) {
                JSONObject petData = (JSONObject) obj;

                String requestBody = petData.toJSONString();

                // Send request to create pet and capture response
                Response response = given()
                                        .contentType("application/json")
                                        .body(requestBody)
                                     .when()
                                        .post("https://petstore.swagger.io/v2/pet");

                // Extract the ID from the response
                int petId = response.then().extract().path("id");
                
                // Add pet ID to the JSONObject
                petIds.put(petData.get("name"), petId);
            }

            // Write the pet IDs to a JSON file
            FileWriter file = new FileWriter("src/test/resources/pet-ids.json");
            file.write(petIds.toJSONString());
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test(dependsOnMethods = { "createPetsAndStoreIDs" }) // Ensure createPetsAndStoreIDs runs first
    void updateExistingPet() {
        try {
            // Update the pet with new data
            JSONObject updatedPetData = new JSONObject();
            updatedPetData.put("id", 5); // Update pet with ID number 1
            updatedPetData.put("name", "BravoSherry"); // Change to the new name as needed
            updatedPetData.put("status", "sold"); // Change to the new status as needed

            given()
                .contentType("application/json")
                .body(updatedPetData.toJSONString())
            .when()
                .put("https://petstore.swagger.io/v2/pet")
            .then()
                .statusCode(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test (priority=3)
    void findPetsByStatus() {
        given()
            .queryParam("status", "available") // specify the status
        .when()
            .get("https://petstore.swagger.io/v2/pet/findByStatus?status=")
        .then()
            .statusCode(200) // Ensure a successful response
            .body("status", Matchers.hasItem("available")) // Ensure at least one pet has status "available"
            .body("id", Matchers.notNullValue()) // Ensure all pets have a non-null ID
            .body("name", Matchers.notNullValue()); // Ensure all pets have a non-null name
    }

    @Test (priority=4)
    void findPetsByTags() {
        given()
            .queryParam("tom", "fed", "rex") // specify the tags
        .when()
            .get("https://petstore.swagger.io/v2/pet/findByTags?tags=")
         .then()
         			.statusCode(200)
         			.body("id", Matchers.everyItem(Matchers.isA(Integer.class)))
                    .body("id", Matchers.notNullValue())
                    .body("name", Matchers.notNullValue()); 
        }
}
