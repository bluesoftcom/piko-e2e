package com.bluesoft.piko_e2e;

import com.bluesoft.piko_e2e.cognito.CognitoUser;
import com.bluesoft.piko_e2e.cognito.CognitoUsers;
import com.bluesoft.piko_e2e.http.JsonResponse;
import com.bluesoft.piko_e2e.piko.PikoAdminClient;
import com.bluesoft.piko_e2e.piko.PikoLocationsClient;
import com.bluesoft.piko_e2e.piko.PikoMapsClient;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.bluesoft.piko_e2e.Uuids.uuid;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@SpringBootTest
class LocationPublishingE2E {

    @Autowired
    private CognitoUsers cognitoUsers;

    @Autowired
    private PikoLocationsClient pikoLocationsClient;

    @Autowired
    private PikoAdminClient pikoAdminClient;

    @Autowired
    private PikoMapsClient pikoMapsClient;

    private static CognitoUser user;
    private static CognitoUser admin;

    @BeforeAll
    static void beforeAllMethods(@Autowired CognitoUsers cognitoUsers) {
        user = cognitoUsers.createUser("john.doe_" + uuid());
        admin = cognitoUsers.createUser("admin_" + uuid());
    }

    @AfterAll
    static void afterAllMethods(@Autowired CognitoUsers cognitoUsers) {
        cognitoUsers.deleteUser(user);
        cognitoUsers.deleteUser(admin);
    }

    @Test
    void testLocationPublicationUserJourney() throws Exception {
        // given user creates a location:
        final JsonResponse createdLocationResponse = pikoLocationsClient.createLocation(
                user,
                JsonNodeFactory.instance.objectNode()
                        .put("name", "Cornflower Museum")
                        .put("lat", 23.2)
                        .put("lng", -123)
        );
        final String locationId = createdLocationResponse.getBody().get("id").asText();

        assertThatJson(createdLocationResponse.getRawBody())
                .inPath("status")
                .isEqualTo("DRAFT");

        // and user can find the location in the listing:
        final JsonResponse locationsListingResponse = pikoLocationsClient.listLocations(user);

        assertThatJson(locationsListingResponse.getRawBody())
                .and(assertion -> assertion.node("items")
                        .isArray()
                        .anySatisfy(item -> {
                            assertThatJson(item).inPath("$.id").isEqualTo(locationId);
                        })
                );

        // and user changes its state to awaiting for publication:
        pikoLocationsClient.changeLocationStatus(
                user,
                locationId,
                JsonNodeFactory.instance.objectNode()
                        .put("status", "AWAITING_PUBLICATION")
        );
        aLittleDelay();

        // and admin finds the location in the list:
        final JsonResponse adminListingResponse = pikoAdminClient.listLocations(admin);
        assertThatJson(adminListingResponse.getRawBody())
                .and(assertion -> assertion.node("items")
                        .isArray()
                        .anySatisfy(item -> {
                            assertThatJson(item).inPath("$.id").isEqualTo(locationId);
                        })
                );

        // and admin changes the location state to published:
        pikoAdminClient.changeLocationStatus(
                user,
                locationId,
                JsonNodeFactory.instance.objectNode()
                        .put("status", "PUBLISHED")
        );
        aLittleDelay();

        // when public-web user lists published locations:
        final JsonResponse publicLocationsListing = pikoMapsClient.listLocations();

        // then public-web user can find published location in the list:
        assertThatJson(publicLocationsListing.getRawBody())
                .and(assertion -> assertion.node("items")
                        .isArray()
                        .anySatisfy(item -> {
                            assertThatJson(item).inPath("$.id").isEqualTo(locationId);
                        })
                );
    }

    @Test
    void testLocationRejectionUserJourney() throws Exception {
        // given user creates a location:
        final JsonResponse createdLocationResponse = pikoLocationsClient.createLocation(
                user,
                JsonNodeFactory.instance.objectNode()
                        .put("name", "Cornflower Museum")
                        .put("lat", 23.2)
                        .put("lng", -123)
        );
        final String locationId = createdLocationResponse.getBody().get("id").asText();

        assertThatJson(createdLocationResponse.getRawBody())
                .inPath("status")
                .isEqualTo("DRAFT");

        // and user can find the location in the listing:
        final JsonResponse locationsListingResponse = pikoLocationsClient.listLocations(user);

        assertThatJson(locationsListingResponse.getRawBody())
                .and(assertion -> assertion.node("items")
                        .isArray()
                        .anySatisfy(item -> {
                            assertThatJson(item).inPath("$.id").isEqualTo(locationId);
                        })
                );

        // and user changes its state to awaiting for publication:
        pikoLocationsClient.changeLocationStatus(
                user,
                locationId,
                JsonNodeFactory.instance.objectNode()
                        .put("status", "AWAITING_PUBLICATION")
        );
        aLittleDelay();

        // and admin finds the location in the list:
        final JsonResponse adminListingResponse = pikoAdminClient.listLocations(admin);
        assertThatJson(adminListingResponse.getRawBody())
                .and(assertion -> assertion.node("items").isArray()
                        .anySatisfy(item -> {
                            assertThatJson(item).inPath("$.id").isEqualTo(locationId);
                        })
                );

        // when admin changes the location state to rejected:
        pikoAdminClient.changeLocationStatus(
                user,
                locationId,
                JsonNodeFactory.instance.objectNode()
                        .put("status", "REJECTED")
        );

        // then user can find the location in the rejected status:
        aLittleDelay();

        final JsonResponse secondLocationsListing = pikoLocationsClient.listLocations(user);
        assertThatJson(secondLocationsListing.getRawBody())
                .and(assertion -> assertion.node("items").isArray()
                        .anySatisfy(item -> {
                            assertThatJson(item)
                                    .and(itemAssertion -> itemAssertion.node("id").isEqualTo(locationId))
                                    .and(itemAssertion -> itemAssertion.node("status").isEqualTo("REJECTED"));
                        })
                );

        // and admin no longer can find the location in the listing
        final JsonResponse secondAdminListingResponse = pikoAdminClient.listLocations(admin);
        assertThatJson(secondAdminListingResponse.getRawBody())
                .and(assertion -> assertion.node("items").isArray()
                        .noneSatisfy(item -> {
                            assertThatJson(item).inPath("$.id").isEqualTo(locationId);
                        })
                );
    }

    private static void aLittleDelay() throws InterruptedException {
        Thread.sleep(1000);
    }
}