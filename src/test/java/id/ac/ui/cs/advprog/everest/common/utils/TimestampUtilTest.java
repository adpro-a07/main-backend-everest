package id.ac.ui.cs.advprog.everest.common.utils;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TimestampUtilTest {

    @Test
    void testToInstantConvertsCorrectly() {
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(1680000000L)
                .setNanos(123456789)
                .build();

        Instant instant = TimestampUtil.toInstant(timestamp);

        assertThat(instant).isNotNull();
        assertThat(instant.getEpochSecond()).isEqualTo(1680000000L);
        assertThat(instant.getNano()).isEqualTo(123456789);
    }

    @Test
    void testToProtoConvertsCorrectly() {
        Instant instant = Instant.ofEpochSecond(1680000000L, 123456789);

        Timestamp timestamp = TimestampUtil.toProto(instant);

        assertThat(timestamp).isNotNull();
        assertThat(timestamp.getSeconds()).isEqualTo(1680000000L);
        assertThat(timestamp.getNanos()).isEqualTo(123456789);
    }
}
