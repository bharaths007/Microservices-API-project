package com.synechron.demo.controller;

import com.synechron.demo.model.UserDto;
import com.synechron.demo.service.MailService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@OpenAPIDefinition(info = @Info(title = "Mail Sender API"))
public class MailController {

    MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    //http://localhost:8082/send
    @PostMapping("/send")
    public String send(@RequestBody UserDto userDto) {
        mailService.mailSend(userDto.getBic()+" "+userDto.getEmail());

        return "Success";
    }


}
