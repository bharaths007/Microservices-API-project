package com.synechron.demo.kafka;

import com.synechron.demo.model.UserDto;
import com.synechron.demo.model.kafka.FlowType;
import com.synechron.demo.model.kafka.UserMailEvent;
import com.synechron.demo.service.MailService;
import org.springframework.stereotype.Component;

@Component
public class MailEventFlowRouter {

    private final MailService mailService;

    public MailEventFlowRouter(MailService mailService) {
        this.mailService = mailService;
    }

    public void process(UserMailEvent event) {
        if (event.flowType() == FlowType.API) {
            processApiFlow(event.payload());
            return;
        }
        processBatchFlow(event.payload());
    }

    private void processApiFlow(UserDto payload) {
        mailService.mailSend(payload.getBic() + " " + payload.getEmail());
    }

    private void processBatchFlow(UserDto payload) {
        mailService.mailSend(payload.getBic() + " " + payload.getEmail());
    }
}
