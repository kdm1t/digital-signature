package com.kdm1t.digsig.rest.controller;

import com.kdm1t.digsig.rest.entity.EmailMessageResponse;
import com.kdm1t.digsig.rest.entity.SendEmailRequest;
import com.kdm1t.digsig.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")
public class MailController {
    private final MailService mailService;

    @PostMapping(value = "/send", produces = MediaType.APPLICATION_JSON_VALUE)
    private SendEmailRequest send(@RequestBody SendEmailRequest request) throws Exception {
        mailService.sendEmail(request);
        return request;
    }

    @GetMapping(value = "/get_messages", produces = MediaType.APPLICATION_JSON_VALUE)
    private List<EmailMessageResponse> getMessages() {
        return mailService.readInboundEmails();
    }

}
