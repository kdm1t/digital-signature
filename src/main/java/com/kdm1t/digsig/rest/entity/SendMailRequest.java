package com.kdm1t.digsig.rest.entity;

import lombok.Data;

@Data
public class SendMailRequest {

    private String to;
    private String subject;
    private String text;
    private String result;

}
