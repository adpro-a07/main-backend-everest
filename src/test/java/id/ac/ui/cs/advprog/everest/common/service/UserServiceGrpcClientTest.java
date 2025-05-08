package id.ac.ui.cs.advprog.everest.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import id.ac.ui.cs.advprog.everest.common.utils.RequestMetadataUtil;
import id.ac.ui.cs.advprog.everest.common.utils.TimestampUtil;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.*;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

public class UserServiceGrpcClientTest {

    private UserServiceGrpc.UserServiceBlockingStub stub;
    private UserServiceGrpcClient client;
    private RequestMetadataUtil metadataUtil;

    @BeforeEach
    void setUp() {
        stub = mock(UserServiceGrpc.UserServiceBlockingStub.class);
        metadataUtil = mock(RequestMetadataUtil.class);
        client = new UserServiceGrpcClient(stub, metadataUtil);

        when(metadataUtil.create()).thenReturn(RequestMetadata.newBuilder().setRequestId("test").build());
    }

    // --- listUsers ---

    @Test
    void listUsers_shouldReturnListUsersResponse() {
        // Create a sample response
        ListUsersResponse response = ListUsersResponse.newBuilder()
                .setTotalCount(10)
                .setTotalPages(2)
                .setCurrentPage(0)
                .addUsers(createSampleUserData())
                .build();

        when(stub.listUsers(any())).thenReturn(response);

        ListUsersResponse actual = client.listUsers(0, 5);

        assertEquals(10, actual.getTotalCount());
        assertEquals(1, actual.getUsersCount());

        // Verify the request was constructed correctly
        verify(stub).listUsers(argThat(request ->
                request.getPageNumber() == 0 &&
                        request.getPageSize() == 5
        ));
    }

    @Test
    void listUsers_shouldThrowIllegalArgumentException_whenPageIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> client.listUsers(-1, 5));
    }

    @Test
    void listUsers_shouldThrowIllegalArgumentException_whenSizeIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> client.listUsers(0, 0));
        assertThrows(IllegalArgumentException.class, () -> client.listUsers(0, -5));
    }

    @Test
    void listUsers_shouldThrowRuntimeException_whenGrpcFails() {
        when(stub.listUsers(any())).thenThrow(StatusRuntimeException.class);
        assertThrows(RuntimeException.class, () -> client.listUsers(0, 5));
    }

    // --- listUsersByRole ---

    @Test
    void listUsersByRole_shouldReturnListUsersResponse() {
        // Create a sample response
        ListUsersResponse response = ListUsersResponse.newBuilder()
                .setTotalCount(5)
                .setTotalPages(1)
                .setCurrentPage(0)
                .addUsers(createSampleUserData())
                .build();

        when(stub.listUsers(any())).thenReturn(response);

        ListUsersResponse actual = client.listUsersByRole(0, 10, UserRole.TECHNICIAN);

        assertEquals(5, actual.getTotalCount());
        assertEquals(1, actual.getUsersCount());

        // Verify the request was constructed correctly
        verify(stub).listUsers(argThat(request ->
                request.getPageNumber() == 0 &&
                        request.getPageSize() == 10 &&
                        request.getRole() == UserRole.TECHNICIAN
        ));
    }

    @Test
    void listUsersByRole_shouldThrowIllegalArgumentException_whenPageIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> client.listUsersByRole(-1, 5, UserRole.CUSTOMER));
    }

    @Test
    void listUsersByRole_shouldThrowIllegalArgumentException_whenSizeIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> client.listUsersByRole(0, 0, UserRole.CUSTOMER));
        assertThrows(IllegalArgumentException.class,
                () -> client.listUsersByRole(0, -5, UserRole.CUSTOMER));
    }

    @Test
    void listUsersByRole_shouldThrowIllegalArgumentException_whenRoleIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> client.listUsersByRole(0, 5, null));
    }

    @Test
    void listUsersByRole_shouldThrowRuntimeException_whenGrpcFails() {
        when(stub.listUsers(any())).thenThrow(StatusRuntimeException.class);
        assertThrows(RuntimeException.class,
                () -> client.listUsersByRole(0, 5, UserRole.CUSTOMER));
    }

    // --- getRandomTechnician ---

    @Test
    void getRandomTechnician_shouldReturnRandomTechnicianResponse() {
        // Create a sample response
        GetRandomTechnicianResponse response = GetRandomTechnicianResponse.newBuilder()
                .setTechnician(createSampleUserData())
                .build();

        when(stub.getRandomTechnician(any())).thenReturn(response);

        GetRandomTechnicianResponse actual = client.getRandomTechnician();

        assertNotNull(actual.getTechnician());
        assertEquals("Test User", actual.getTechnician().getIdentity().getFullName());

        // Verify the request was constructed correctly
        verify(stub).getRandomTechnician(any());
    }

    @Test
    void getRandomTechnician_shouldThrowRuntimeException_whenGrpcFails() {
        when(stub.getRandomTechnician(any())).thenThrow(StatusRuntimeException.class);
        assertThrows(RuntimeException.class, () -> client.getRandomTechnician());
    }

    // --- Helper methods ---

    private UserData createSampleUserData() {
        return UserData.newBuilder()
                .setIdentity(UserIdentity.newBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setEmail("user@test.com")
                        .setFullName("Test User")
                        .setRole(UserRole.TECHNICIAN)
                        .setPhoneNumber("123456789")
                        .setCreatedAt(TimestampUtil.toProto(Instant.now()))
                        .setUpdatedAt(TimestampUtil.toProto(Instant.now()))
                )
                .setProfile(UserProfile.newBuilder()
                        .setAddress("Somewhere")
                        .setWorkExperience("5 years")
                        .setTotalJobsDone(42)
                        .setTotalIncome(50000)
                )
                .build();
    }
}