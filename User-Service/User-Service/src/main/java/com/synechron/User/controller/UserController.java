package com.synechron.User.controller;

import com.synechron.User.model.User;
import com.synechron.User.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api")
    public String sendApiUser(@RequestBody User user) {
        userService.publishApiFlow(user);
        return "Accepted for API flow";
    }

    @PostMapping("/batch")
    public String sendBatchUser(@RequestBody User user) {
        userService.publishBatchFlow(user);
        return "Accepted for Batch flow";
    }
}
