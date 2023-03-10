package com.kdm1t.digsig.rest.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@ToString
public class EmailMessageResponse implements EmailStatus {

    private Integer number;
    private String from;
    private String subject;
    private String message;
    private LocalDateTime messageDate;
    private String checkStatus;

    public EmailMessageResponse(Integer number, String from, String subject, String message, LocalDateTime messageDate) {
        this.number = number;
        this.from = from;
        this.subject = subject;
        this.message = message;
        this.messageDate = messageDate;
    }
}
