package com.bluesoft.piko_e2e.cognito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
@Slf4j
public class CognitoUsers {

    private final AWSCognitoIdentityProvider cognito;
    private final CognitoProperties cognitoProperties;

    public CognitoUser createUser(String username) {
        log.info("Creating new user: {}", username);
        final String email = String.format("success+%s@simulator.amazonses.com", username);

        final String initialPassword = nextPassword();
        cognito.adminCreateUser(
                toCreateUserRequest(username, initialPassword, email, cognitoProperties)
        );

        final AdminInitiateAuthResult authResult = cognito.adminInitiateAuth(
                toInitiateAuthRequest(username, initialPassword, cognitoProperties)
        );

        final String password = nextPassword();
        final RespondToAuthChallengeResult challengeResult = cognito.respondToAuthChallenge(
                toRespondToChallengeRequest(username, password, authResult, cognitoProperties)
        );

        final String idToken = challengeResult.getAuthenticationResult().getIdToken();
        return new CognitoUser(username, password, email, idToken);
    }

    public void deleteUser(CognitoUser user) {
        log.info("Deleting user: {}", user.getUsername());
        cognito.adminDeleteUser(
                new AdminDeleteUserRequest()
                        .withUsername(user.getUsername())
                        .withUserPoolId(cognitoProperties.getUserPoolId())
        );
    }

    private static RespondToAuthChallengeRequest toRespondToChallengeRequest(String username,
                                                                             String newPassword,
                                                                             AdminInitiateAuthResult authResult,
                                                                             CognitoProperties cognitoProperties) {
        return new RespondToAuthChallengeRequest()
                .withSession(authResult.getSession())
                .withClientId(cognitoProperties.getAppClientId())
                .withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                .withChallengeResponses(
                        Map.of("USERNAME", username, "NEW_PASSWORD", newPassword)
                );
    }

    private static AdminInitiateAuthRequest toInitiateAuthRequest(String username, String password, CognitoProperties cognitoProperties) {
        return new AdminInitiateAuthRequest()
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withUserPoolId(cognitoProperties.getUserPoolId())
                .withClientId(cognitoProperties.getAppClientId())
                .withAuthParameters(
                        Map.of("USERNAME", username, "PASSWORD", password)
                );
    }

    private static AdminCreateUserRequest toCreateUserRequest(String username,
                                                              String initialPassword,
                                                              String email,
                                                              CognitoProperties cognitoProperties) {
        return new AdminCreateUserRequest()
                .withUserPoolId(cognitoProperties.getUserPoolId())
                .withUsername(username)
                .withTemporaryPassword(initialPassword)
                .withUserAttributes(
                        new AttributeType()
                                .withName("email")
                                .withValue(email),
                        new AttributeType()
                                .withName("email_verified")
                                .withValue("true")
                );
    }

    private static String nextPassword() {
        return String.format("Password@%s", (int) (Math.random() * 100));
    }

}
