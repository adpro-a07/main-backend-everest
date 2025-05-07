package id.ac.ui.cs.advprog.everest.common.utils;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.RequestMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RequestMetadataUtilTest {

    private RequestMetadataUtil requestMetadataUtil;

    @BeforeEach
    void setUp() {
        requestMetadataUtil = new RequestMetadataUtil();
        ReflectionTestUtils.setField(requestMetadataUtil, "applicationVersion", "9.9.9");
    }

    @Test
    void testCreateGeneratesAllFields() {
        RequestMetadata metadata = requestMetadataUtil.create();

        assertThat(metadata.getRequestId()).isNotEmpty();
        assertThat(metadata.getCorrelationId()).isNotEmpty();
        assertThat(metadata.getClientVersion()).isEqualTo("9.9.9");
    }

    @Test
    void testCreateWithVersionOverridesVersion() {
        RequestMetadata metadata = requestMetadataUtil.createWithVersion("2.3.4");

        assertThat(metadata.getClientVersion()).isEqualTo("2.3.4");
        assertThat(metadata.getRequestId()).isNotEmpty();
        assertThat(metadata.getCorrelationId()).isNotEmpty();
    }

    @Test
    void testCreateWithRequestIdSetsCorrectRequestId() {
        String customId = UUID.randomUUID().toString();
        RequestMetadata metadata = requestMetadataUtil.createWithRequestId(customId);

        assertThat(metadata.getRequestId()).isEqualTo(customId);
        assertThat(metadata.getClientVersion()).isEqualTo("9.9.9");
        assertThat(metadata.getCorrelationId()).isNotEmpty();
    }

    @Test
    void testCreateWithCorrelationIdSetsCorrectCorrelationId() {
        String correlationId = UUID.randomUUID().toString();
        RequestMetadata metadata = requestMetadataUtil.createWithCorrelationId(correlationId);

        assertThat(metadata.getCorrelationId()).isEqualTo(correlationId);
        assertThat(metadata.getRequestId()).isNotEmpty();
        assertThat(metadata.getClientVersion()).isEqualTo("9.9.9");
    }

    @Test
    void testCreateCustomSetsAllFields() {
        String requestId = "req-123";
        String clientVersion = "1.2.3";
        String correlationId = "corr-456";

        RequestMetadata metadata = requestMetadataUtil.createCustom(requestId, clientVersion, correlationId);

        assertThat(metadata.getRequestId()).isEqualTo(requestId);
        assertThat(metadata.getClientVersion()).isEqualTo(clientVersion);
        assertThat(metadata.getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    void testCreateForChainedRequestKeepsRequestIdChangesCorrelationId() {
        RequestMetadata original = requestMetadataUtil.create();
        RequestMetadata chained = requestMetadataUtil.createForChainedRequest(original);

        assertThat(chained.getRequestId()).isEqualTo(original.getRequestId());
        assertThat(chained.getCorrelationId()).isNotEqualTo(original.getCorrelationId());
        assertThat(chained.getClientVersion()).isEqualTo("9.9.9");
    }
}
