package com.bluesoft.piko_e2e;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PikoE2EConfiguration {

    @Bean
    public AWSCognitoIdentityProvider awsCognitoIdentityProvider() {
        return AWSCognitoIdentityProviderClient.builder()
                .withCredentials(new ProfileCredentialsProvider(".aws/credentials", null))
                .withRegion(Regions.EU_WEST_1)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
