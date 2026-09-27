package org.images.services.impl;

import lombok.AllArgsConstructor;
import org.images.config.CognitoConfig;
import org.images.dtos.*;
import org.images.exceptions.InvalidOperationException;
import org.images.services.AuthService;
import org.springframework.http.HttpStatus;
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
        var signUpRequest = SignUpRequest.builder()
                .clientId(cognitoConfig.getClientId())
                .username(request.email())
                .password(request.password())
                .userAttributes(
                        AttributeType.builder()
                                .name("email")
                                .value(request.email())
                                .build(),
                        AttributeType.builder()
                                .name("name")
                                .value(request.name())
                                .build()
                )
                .build();

        cognitoClient.signUp(signUpRequest);
        return new RegisterUserResponse(
                "User created successfully. Please check your email for verification code."
        );
    }

    @Override
    public VerificationResponse confirmEmail(ConfirmEmailRequest request) {
        try {
            var confirmRequest = ConfirmSignUpRequest.builder()
                    .clientId(cognitoConfig.getClientId())
                    .username(request.email())
                    .confirmationCode(request.code())
                    .build();

            cognitoClient.confirmSignUp(confirmRequest);
            return new VerificationResponse("Email verified successfully. You can now login");
        } catch (CodeMismatchException e) {
            throw new InvalidOperationException("Invalid verification code", HttpStatus.BAD_REQUEST);
        } catch (ExpiredCodeException e) {
            throw new InvalidOperationException("Verification code has expired", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public VerificationResponse resendConfirmationCode(String email) {
        var resendRequest = ResendConfirmationCodeRequest.builder()
                .clientId(cognitoConfig.getClientId())
                .username(email)
                .build();

        cognitoClient.resendConfirmationCode(resendRequest);

        return new VerificationResponse("Verification code sent to your email.");
    }
}
