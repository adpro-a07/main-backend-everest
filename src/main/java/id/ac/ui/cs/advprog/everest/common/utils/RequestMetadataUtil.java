package id.ac.ui.cs.advprog.everest.common.utils;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.RequestMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Utility class for creating and managing RequestMetadata for gRPC calls.
 */
@Component
public class RequestMetadataUtil {

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    /**
     * Creates a new RequestMetadata with default values.
     * - Generates a random UUID for request_id
     * - Uses the application version from properties
     * - Generates a random UUID for correlation_id
     *
     * @return a new RequestMetadata instance
     */
    public RequestMetadata create() {
        return RequestMetadata.newBuilder()
                .setRequestId(UUID.randomUUID().toString())
                .setClientVersion(applicationVersion)
                .setCorrelationId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Creates a new RequestMetadata with the specified client version.
     *
     * @param clientVersion the client version to use
     * @return a new RequestMetadata instance
     */
    public RequestMetadata createWithVersion(String clientVersion) {
        return RequestMetadata.newBuilder()
                .setRequestId(UUID.randomUUID().toString())
                .setClientVersion(clientVersion)
                .setCorrelationId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Creates a new RequestMetadata with a specific request ID.
     * Useful for tracking specific requests.
     *
     * @param requestId the request ID to use
     * @return a new RequestMetadata instance
     */
    public RequestMetadata createWithRequestId(String requestId) {
        return RequestMetadata.newBuilder()
                .setRequestId(requestId)
                .setClientVersion(applicationVersion)
                .setCorrelationId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Creates a new RequestMetadata with a specific correlation ID.
     * Useful for distributed tracing.
     *
     * @param correlationId the correlation ID to use
     * @return a new RequestMetadata instance
     */
    public RequestMetadata createWithCorrelationId(String correlationId) {
        return RequestMetadata.newBuilder()
                .setRequestId(UUID.randomUUID().toString())
                .setClientVersion(applicationVersion)
                .setCorrelationId(correlationId)
                .build();
    }

    /**
     * Creates a new RequestMetadata with custom values.
     *
     * @param requestId the request ID to use
     * @param clientVersion the client version to use
     * @param correlationId the correlation ID to use
     * @return a new RequestMetadata instance
     */
    public RequestMetadata createCustom(String requestId, String clientVersion, String correlationId) {
        return RequestMetadata.newBuilder()
                .setRequestId(requestId)
                .setClientVersion(clientVersion)
                .setCorrelationId(correlationId)
                .build();
    }

    /**
     * Creates a new RequestMetadata with the request ID from an existing request
     * but generates a new correlation ID. Useful for chaining related requests.
     *
     * @param originalMetadata the original metadata to derive the request ID from
     * @return a new RequestMetadata instance
     */
    public RequestMetadata createForChainedRequest(RequestMetadata originalMetadata) {
        return RequestMetadata.newBuilder()
                .setRequestId(originalMetadata.getRequestId())
                .setClientVersion(applicationVersion)
                .setCorrelationId(UUID.randomUUID().toString())
                .build();
    }
}