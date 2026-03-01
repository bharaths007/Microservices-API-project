package com.synechron.User.controller;

import com.synechron.User.model.FlowType;
import com.synechron.User.model.User;
import com.synechron.User.service.UserEventProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserEventProducerService userEventProducerService;

    @PostMapping("/api")
    public Map<String, String> sendApiUser(@RequestBody User user) {
        String eventId = userEventProducerService.publish(user, FlowType.API);
        return Map.of("status", "ACCEPTED", "flow", "API", "eventId", eventId);
    }

    @PostMapping("/batch")
    public Map<String, String> sendBatchUser(@RequestBody User user) {
        String eventId = userEventProducerService.publish(user, FlowType.BATCH);
        return Map.of("status", "ACCEPTED", "flow", "BATCH", "eventId", eventId);
    }
}
