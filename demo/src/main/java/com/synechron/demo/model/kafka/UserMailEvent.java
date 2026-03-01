package com.synechron.demo.model.kafka;

import com.synechron.demo.model.UserDto;

public record UserMailEvent(
        String eventId,
        long createdAtEpochMs,
        FlowType flowType,
        UserDto payload
) {
}
