package com.bluesoft.piko_e2e.cognito;

import lombok.ToString;
import lombok.Value;

@Value
public class CognitoUser {

    String username;

    @ToString.Exclude
    String password;

    String email;

    @ToString.Exclude
    String idToken;

}
