package com.bluesoft.piko_e2e.cognito;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("cognito")
@Data
@Component
public class CognitoProperties {

    private String userPoolId;
    private String appClientId;

}
