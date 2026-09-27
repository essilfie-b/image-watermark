package org.images.services;

import org.images.dtos.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

public interface AuthService {

    AuthenticationResultType login(LoginRequest request);

    AuthenticationResultType refreshToken(String refreshToken);

    RegisterUserResponse createUser(RegisterUserRequest request);

    VerificationResponse confirmEmail(ConfirmEmailRequest request);

    VerificationResponse resendConfirmationCode(String email);
}
