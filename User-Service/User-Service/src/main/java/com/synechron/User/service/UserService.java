package com.synechron.User.service;

import com.synechron.User.kafka.UserEventPublisher;
import com.synechron.User.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserEventPublisher userEventPublisher;

    public UserService(UserEventPublisher userEventPublisher) {
        this.userEventPublisher = userEventPublisher;
    }

    public void publishApiFlow(User user) {
        userEventPublisher.publishApi(user);
    }

    public void publishBatchFlow(User user) {
        userEventPublisher.publishBatch(user);
    }
}
