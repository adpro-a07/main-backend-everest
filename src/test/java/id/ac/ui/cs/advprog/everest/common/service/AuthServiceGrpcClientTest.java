package id.ac.ui.cs.advprog.everest.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.authentication.exception.AuthServiceException;
import id.ac.ui.cs.advprog.everest.authentication.exception.InvalidTokenException;
import id.ac.ui.cs.advprog.everest.common.utils.RequestMetadataUtil;
import id.ac.ui.cs.advprog.everest.common.utils.TimestampUtil;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.*;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AuthServiceGrpcClientTest {

    private AuthServiceGrpc.AuthServiceBlockingStub stub;
    private AuthServiceGrpcClient client;

    @BeforeEach
    void setUp() {
        stub = mock(AuthServiceGrpc.AuthServiceBlockingStub.class);
        RequestMetadataUtil metadataUtil = mock(RequestMetadataUtil.class);
        client = new AuthServiceGrpcClient(stub, metadataUtil);

        when(metadataUtil.create()).thenReturn(RequestMetadata.newBuilder().setRequestId("test").build());
    }

    // --- validateToken ---

    @Test
    void validateToken_shouldReturnAuthenticatedUser_whenTokenIsValid() {
        TokenValidationResponse response = TokenValidationResponse.newBuilder()
                .setValid(true)
                .setUserData(UserData.newBuilder()
                        .setIdentity(UserIdentity.newBuilder()
                                .setId(UUID.randomUUID().toString())
                                .setEmail("user@test.com")
                                .setFullName("Test User")
                                .setRole(UserRole.CUSTOMER)
                                .setPhoneNumber("123456789")
                                .setCreatedAt(TimestampUtil.toProto(Instant.now()))
                                .setUpdatedAt(TimestampUtil.toProto(Instant.now()))
                        )
                        .setProfile(UserProfile.newBuilder()
                                .setAddress("Somewhere")
                                .setWorkExperience("None")
                                .setTotalJobsDone(1)
                                .setTotalIncome(1000)
                        )
                )
                .build();

        when(stub.validateToken(any())).thenReturn(response);

        AuthenticatedUser user = client.validateToken("valid-token");
        assertEquals("user@test.com", user.email());
    }

    @Test
    void validateToken_shouldThrowInvalidTokenException_whenTokenInvalid() {
        when(stub.validateToken(any())).thenReturn(
                TokenValidationResponse.newBuilder().setValid(false).build()
        );

        assertThrows(InvalidTokenException.class, () -> client.validateToken("invalid-token"));
    }

    @Test
    void validateToken_shouldThrowAuthServiceException_whenGrpcFails() {
        when(stub.validateToken(any())).thenThrow(StatusRuntimeException.class);
        assertThrows(AuthServiceException.class, () -> client.validateToken("token"));
    }

    @Test
    void validateToken_shouldThrowIllegalArgumentException_whenTokenIsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> client.validateToken(null));
        assertThrows(IllegalArgumentException.class, () -> client.validateToken("  "));
    }

    // --- refreshToken ---

    @Test
    void refreshToken_shouldReturnTokenResponse() {
        TokenRefreshResponse response = TokenRefreshResponse.newBuilder()
                .setAccessToken("new-access")
                .setRefreshToken("new-refresh")
                .setExpiresIn(3600)
                .build();

        when(stub.refreshToken(any())).thenReturn(response);
        TokenRefreshResponse actual = client.refreshToken("refresh-token");

        assertEquals("new-access", actual.getAccessToken());
    }

    @Test
    void refreshToken_shouldThrowOnInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> client.refreshToken(null));
        assertThrows(IllegalArgumentException.class, () -> client.refreshToken(" "));
    }

    @Test
    void refreshToken_shouldThrowOnGrpcError() {
        when(stub.refreshToken(any())).thenThrow(StatusRuntimeException.class);
        assertThrows(AuthServiceException.class, () -> client.refreshToken("token"));
    }

    // --- lookupUserById ---

    @Test
    void lookupUserById_shouldReturnUserResponse() {
        UserLookupResponse response = UserLookupResponse.newBuilder().build();
        when(stub.lookupUser(any())).thenReturn(response);
        assertEquals(response, client.lookupUserById("some-id"));
    }

    @Test
    void lookupUserById_shouldThrowOnNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> client.lookupUserById(null));
        assertThrows(IllegalArgumentException.class, () -> client.lookupUserById(" "));
    }

    @Test
    void lookupUserById_shouldThrowOnGrpcFailure() {
        when(stub.lookupUser(any())).thenThrow(StatusRuntimeException.class);
        assertThrows(AuthServiceException.class, () -> client.lookupUserById("id"));
    }

    // --- lookupUserByEmail ---

    @Test
    void lookupUserByEmail_shouldReturnUserResponse() {
        UserLookupResponse response = UserLookupResponse.newBuilder().build();
        when(stub.lookupUser(any())).thenReturn(response);
        assertEquals(response, client.lookupUserByEmail("email@example.com"));
    }

    @Test
    void lookupUserByEmail_shouldThrowOnNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> client.lookupUserByEmail(null));
        assertThrows(IllegalArgumentException.class, () -> client.lookupUserByEmail(" "));
    }

    @Test
    void lookupUserByEmail_shouldThrowOnGrpcFailure() {
        when(stub.lookupUser(any())).thenThrow(StatusRuntimeException.class);
        assertThrows(AuthServiceException.class, () -> client.lookupUserByEmail("email@example.com"));
    }

    // --- batchLookupUsersByIds ---

    @Test
    void batchLookupUsersByIds_shouldReturnResponse() {
        BatchUserLookupResponse response = BatchUserLookupResponse.newBuilder().build();
        when(stub.batchLookupUsers(any())).thenReturn(response);

        assertEquals(response, client.batchLookupUsersByIds(List.of("id1", "id2"), true));
    }

    @Test
    void batchLookupUsersByIds_shouldThrowOnEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> client.batchLookupUsersByIds(List.of(), true));
    }

    @Test
    void batchLookupUsersByIds_shouldThrowOnGrpcFailure() {
        when(stub.batchLookupUsers(any())).thenThrow(StatusRuntimeException.class);
        assertThrows(AuthServiceException.class, () -> client.batchLookupUsersByIds(List.of("id"), true));
    }

    // --- batchLookupUsersByEmails ---

    @Test
    void batchLookupUsersByEmails_shouldReturnResponse() {
        BatchUserLookupResponse response = BatchUserLookupResponse.newBuilder().build();
        when(stub.batchLookupUsers(any())).thenReturn(response);

        assertEquals(response, client.batchLookupUsersByEmails(List.of("a@a.com", "b@b.com"), false));
    }

    @Test
    void batchLookupUsersByEmails_shouldThrowOnEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> client.batchLookupUsersByEmails(List.of(), false));
    }

    @Test
    void batchLookupUsersByEmails_shouldThrowOnGrpcFailure() {
        when(stub.batchLookupUsers(any())).thenThrow(StatusRuntimeException.class);
        assertThrows(AuthServiceException.class, () -> client.batchLookupUsersByEmails(List.of("a@a.com"), true));
    }
}

