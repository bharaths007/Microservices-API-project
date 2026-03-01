package com.synechron.User.service;

import com.synechron.User.model.User;
import com.synechron.User.kafka.UserEventProducer;
import com.synechron.User.model.kafka.FlowType;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UserService {

    private final UserEventProducer userEventProducer;

    public UserService(UserEventProducer userEventProducer) {
        this.userEventProducer = userEventProducer;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WebClient webClient() {
        return  WebClient.create();
    }

    public void publishToKafka(User user, FlowType flowType) {
        userEventProducer.publish(user, flowType);
    }


}
