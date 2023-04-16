package com.synechron.User.controller;

import com.synechron.User.model.User;
import com.synechron.User.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/user")
    public String sendUser(@RequestBody User user) {
        userService.invokeMailService(user);
        return "Success";
    }
}
