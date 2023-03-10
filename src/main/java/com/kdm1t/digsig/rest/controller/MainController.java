package com.kdm1t.digsig.rest.controller;

import com.kdm1t.digsig.rest.entity.SendEmailRequest;
import com.kdm1t.digsig.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MailService mailService;

    @RequestMapping(value = "/sender", method = RequestMethod.GET)
    public String getIndex(Model model) {
        model.addAttribute("email", new SendEmailRequest());
        return "sender";
    }

    @RequestMapping(value = "/send_email", method = RequestMethod.POST)
    public String sendEmail(@ModelAttribute SendEmailRequest request, Model model) {
        model.addAttribute("email", request);
        try {
            mailService.sendEmail(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "sender";
    }

    @RequestMapping(value = "/receiver", method = RequestMethod.GET)
    public String getMessages(Model model) {
        model.addAttribute("messages", mailService.readInboundEmails());
        return "receiver";
    }

}
