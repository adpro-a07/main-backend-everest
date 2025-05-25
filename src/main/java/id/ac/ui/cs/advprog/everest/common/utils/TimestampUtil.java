package id.ac.ui.cs.advprog.everest.common.utils;

import com.google.protobuf.Timestamp;

import java.time.Instant;

public class TimestampUtil {
    public static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    public static Timestamp toProto(Instant instant) {
        if (instant == null) {
            return null;
        }
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}

