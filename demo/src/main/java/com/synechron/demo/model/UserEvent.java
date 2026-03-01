package com.synechron.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEvent {

    private UUID eventId;
    private String flowType;
    private String bic;
    private String email;
    private Instant createdAt;
}
