package org.images.services;

import org.images.dtos.LoginRequest;
import org.images.dtos.RegisterUserRequest;
import org.images.dtos.RegisterUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

public interface AuthService {

    AuthenticationResultType login(LoginRequest request);

    AuthenticationResultType refreshToken(String refreshToken);

    RegisterUserResponse createUser(RegisterUserRequest request);

}
