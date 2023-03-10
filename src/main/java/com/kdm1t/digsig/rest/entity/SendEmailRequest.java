package com.kdm1t.digsig.rest.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class SendEmailRequest {

    private String to;
    private String subject;
    private String message;

}
