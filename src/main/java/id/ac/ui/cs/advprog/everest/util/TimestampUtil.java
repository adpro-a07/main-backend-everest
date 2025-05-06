package id.ac.ui.cs.advprog.everest.util;

import com.google.protobuf.Timestamp;

import java.time.Instant;

public class TimestampUtil {
    public static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}

