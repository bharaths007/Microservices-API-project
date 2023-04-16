package com.synechron.User.service;

import com.synechron.User.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UserService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    WebClient webClient;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WebClient webClient() {
        return  WebClient.create();
    }

    public String invokeMailService(User user) {
        String responseFromMail = restTemplate.postForObject("http://localhost:8082/send",user,String.class);
        //String r = String.valueOf(webClient.post());

        return responseFromMail;

    }


}
