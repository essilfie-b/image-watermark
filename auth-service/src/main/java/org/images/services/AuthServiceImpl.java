package org.images.services;

import lombok.AllArgsConstructor;
import org.images.config.CognitoConfig;
import org.images.dtos.LoginRequest;
import org.images.dtos.RegisterUserRequest;
import org.images.dtos.RegisterUserResponse;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoConfig cognitoConfig;

    @Override
    public AuthenticationResultType login(LoginRequest request) {
        var authRequest = AdminInitiateAuthRequest.builder()
                .userPoolId(cognitoConfig.getUserPoolId())
                .clientId(cognitoConfig.getClientId())
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(Map.of(
                        "USERNAME", request.email(),
                        "PASSWORD", request.password()
                ))
                .build();

        var response = cognitoClient.adminInitiateAuth(authRequest);

        return response.authenticationResult();

    }

    @Override
    public AuthenticationResultType refreshToken(String refreshToken) {
        var authRequest = AdminInitiateAuthRequest.builder()
                .userPoolId(cognitoConfig.getUserPoolId())
                .clientId(cognitoConfig.getClientId())
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .authParameters(Map.of("REFRESH_TOKEN", refreshToken))
                .build();

        var response = cognitoClient.adminInitiateAuth(authRequest);
        return response.authenticationResult();

    }

    @Override
    public RegisterUserResponse createUser(RegisterUserRequest request) {
        var createUserRequest = AdminCreateUserRequest.builder()
                .userPoolId(cognitoConfig.getUserPoolId())
                .username(request.email())
                .userAttributes(
                        AttributeType.builder()
                                .name("email")
                                .value(request.email())
                                .build(),
                        AttributeType.builder()
                                .name("name")
                                .value(request.name())
                                .build(),
                        AttributeType.builder()
                                .name("email_verified")
                                .value("true")
                                .build()
                )
                .temporaryPassword(request.password())
                .messageAction(MessageActionType.SUPPRESS)
                .build();

        var createResponse = cognitoClient.adminCreateUser(createUserRequest);
        String userId = createResponse.user().username();

        var setPasswordRequest = AdminSetUserPasswordRequest.builder()
                .userPoolId(cognitoConfig.getUserPoolId())
                .username(userId)
                .password(request.password())
                .permanent(true)
                .build();

        cognitoClient.adminSetUserPassword(setPasswordRequest);

        return new RegisterUserResponse("User created successfully");
    }
}
