package id.ac.ui.cs.advprog.everest.common.service;

import id.ac.ui.cs.advprog.everest.common.utils.RequestMetadataUtil;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.GetRandomTechnicianRequest;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.GetRandomTechnicianResponse;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.ListUsersRequest;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.ListUsersResponse;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserServiceGrpc;
import io.grpc.StatusRuntimeException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Client for interacting with the User service via gRPC.
 */
@Service
@AllArgsConstructor
public class UserServiceGrpcClient {
    private final UserServiceGrpc.UserServiceBlockingStub stub;
    private final RequestMetadataUtil metadataUtil;

    /**
     * Lists users with pagination.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return the list users response
     * @throws RuntimeException if there's an issue communicating with the user service
     */
    public ListUsersResponse listUsers(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must not be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }

        try {
            return stub.listUsers(
                    ListUsersRequest.newBuilder()
                            .setMetadata(metadataUtil.create())
                            .setPageNumber(page)
                            .setPageSize(size)
                            .build()
            );
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to list users", e);
        }
    }

    /**
     * Lists users with pagination and role filtering.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @param role the user role to filter by
     * @return the list users response
     * @throws RuntimeException if there's an issue communicating with the user service
     */
    public ListUsersResponse listUsersByRole(int page, int size, UserRole role) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must not be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }

        try {
            return stub.listUsers(
                    ListUsersRequest.newBuilder()
                            .setMetadata(metadataUtil.create())
                            .setPageNumber(page)
                            .setPageSize(size)
                            .setRole(role)
                            .build()
            );
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to list users by role", e);
        }
    }

    /**
     * Gets a random technician.
     *
     * @return the random technician response
     * @throws RuntimeException if there's an issue communicating with the user service
     */
    public GetRandomTechnicianResponse getRandomTechnician() {
        try {
            return stub.getRandomTechnician(
                    GetRandomTechnicianRequest.newBuilder()
                            .setMetadata(metadataUtil.create())
                            .build()
            );
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to get random technician", e);
        }
    }
}