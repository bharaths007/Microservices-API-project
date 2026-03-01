package com.synechron.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {

    private String eventId;
    private String bic;
    private String email;
    private FlowType flowType;
    private Instant createdAt;
}
