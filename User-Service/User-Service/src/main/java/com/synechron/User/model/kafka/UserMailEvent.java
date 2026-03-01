package com.synechron.User.model.kafka;

import com.synechron.User.model.User;

public record UserMailEvent(
        String eventId,
        long createdAtEpochMs,
        FlowType flowType,
        User payload
) {
}
