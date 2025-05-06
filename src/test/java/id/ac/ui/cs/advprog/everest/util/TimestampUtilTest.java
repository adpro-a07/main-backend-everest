package id.ac.ui.cs.advprog.everest.util;

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
}
