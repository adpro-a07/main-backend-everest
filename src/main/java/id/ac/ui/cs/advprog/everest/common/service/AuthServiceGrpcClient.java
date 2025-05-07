package id.ac.ui.cs.advprog.everest.common.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.authentication.exception.AuthServiceException;
import id.ac.ui.cs.advprog.everest.authentication.exception.InvalidTokenException;
import id.ac.ui.cs.advprog.everest.common.utils.RequestMetadataUtil;
import id.ac.ui.cs.advprog.everest.common.utils.TimestampUtil;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.AuthServiceGrpc;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.BatchUserLookupRequest;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.BatchUserLookupResponse;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.TokenRefreshRequest;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.TokenRefreshResponse;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.TokenValidationRequest;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.TokenValidationResponse;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserIdentifier;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserLookupRequest;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserLookupResponse;
import io.grpc.StatusRuntimeException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthServiceGrpcClient {
    private final AuthServiceGrpc.AuthServiceBlockingStub stub;
    private final RequestMetadataUtil metadataUtil;

    /**
     * Validates a token and returns the authenticated user.
     *
     * @param token the token to validate
     * @return the authenticated user
     * @throws InvalidTokenException if the token is invalid
     * @throws AuthServiceException if there's an issue communicating with the auth service
     */
    public AuthenticatedUser validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token must not be null or blank");
        }
        try {
            TokenValidationResponse response = stub.validateToken(
                    TokenValidationRequest.newBuilder()
                            .setMetadata(metadataUtil.create())
                            .setToken(token)
                            .setIncludeUserData(true)
                            .build()
            );
            if (!response.getValid()) {
                throw new InvalidTokenException("Invalid token");
            }
            var identity = response.getUserData().getIdentity();
            var profile = response.getUserData().getProfile();
            return new AuthenticatedUser(
                    UUID.fromString(identity.getId()),
                    identity.getEmail(),
                    identity.getFullName(),
                    identity.getRole(),
                    identity.getPhoneNumber(),
                    TimestampUtil.toInstant(identity.getCreatedAt()),
                    TimestampUtil.toInstant(identity.getUpdatedAt()),
                    profile.getAddress(),
                    profile.getWorkExperience(),
                    profile.getTotalJobsDone(),
                    profile.getTotalIncome()
            );
        } catch (StatusRuntimeException e) {
            throw new AuthServiceException("Failed to communicate with Auth service", e);
        }
    }

    /**
     * Refreshes an access token.
     *
     * @param refreshToken the refresh token
     * @return the token refresh response
     * @throws AuthServiceException if there's an issue communicating with the auth service
     */
    public TokenRefreshResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token must not be null or blank");
        }
        try {
            return stub.refreshToken(
                    TokenRefreshRequest.newBuilder()
                            .setMetadata(metadataUtil.create())
                            .setRefreshToken(refreshToken)
                            .build()
            );
        } catch (StatusRuntimeException e) {
            throw new AuthServiceException("Failed to refresh token", e);
        }
    }

    /**
     * Looks up a user by ID.
     *
     * @param userId the user ID
     * @return the user lookup response
     * @throws AuthServiceException if there's an issue communicating with the auth service
     */
    public UserLookupResponse lookupUserById(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be null or blank");
        }
        try {
            return stub.lookupUser(
                    UserLookupRequest.newBuilder()
                            .setMetadata(metadataUtil.create())
                            .setUserId(userId)
                            .build()
            );
        } catch (StatusRuntimeException e) {
            throw new AuthServiceException("Failed to lookup user by ID", e);
        }
    }

    /**
     * Looks up a user by email.
     *
     * @param email the user email
     * @return the user lookup response
     * @throws AuthServiceException if there's an issue communicating with the auth service
     */
    public UserLookupResponse lookupUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }
        try {
            return stub.lookupUser(
                    UserLookupRequest.newBuilder()
                            .setMetadata(metadataUtil.create())
                            .setEmail(email)
                            .build()
            );
        } catch (StatusRuntimeException e) {
            throw new AuthServiceException("Failed to lookup user by email", e);
        }
    }

    /**
     * Batch lookup of users by IDs.
     *
     * @param userIds the list of user IDs
     * @param includeProfile whether to include profile data
     * @return the batch user lookup response
     * @throws AuthServiceException if there's an issue communicating with the auth service
     */
    public BatchUserLookupResponse batchLookupUsersByIds(List<String> userIds, boolean includeProfile) {
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("User IDs list must not be null or empty");
        }
        try {
            BatchUserLookupRequest.Builder requestBuilder = BatchUserLookupRequest.newBuilder()
                    .setMetadata(metadataUtil.create())
                    .setIncludeProfile(includeProfile);

            for (String userId : userIds) {
                requestBuilder.addIdentifiers(
                        UserIdentifier.newBuilder().setUserId(userId).build()
                );
            }

            return stub.batchLookupUsers(requestBuilder.build());
        } catch (StatusRuntimeException e) {
            throw new AuthServiceException("Failed to batch lookup users by IDs", e);
        }
    }

    /**
     * Batch lookup of users by emails.
     *
     * @param emails the list of user emails
     * @param includeProfile whether to include profile data
     * @return the batch user lookup response
     * @throws AuthServiceException if there's an issue communicating with the auth service
     */
    public BatchUserLookupResponse batchLookupUsersByEmails(List<String> emails, boolean includeProfile) {
        if (emails == null || emails.isEmpty()) {
            throw new IllegalArgumentException("Emails list must not be null or empty");
        }
        try {
            BatchUserLookupRequest.Builder requestBuilder = BatchUserLookupRequest.newBuilder()
                    .setMetadata(metadataUtil.create())
                    .setIncludeProfile(includeProfile);

            for (String email : emails) {
                requestBuilder.addIdentifiers(
                        UserIdentifier.newBuilder().setEmail(email).build()
                );
            }

            return stub.batchLookupUsers(requestBuilder.build());
        } catch (StatusRuntimeException e) {
            throw new AuthServiceException("Failed to batch lookup users by emails", e);
        }
    }
}