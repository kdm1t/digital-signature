package com.kdm1t.digsig.rest.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.nio.file.Path;

@Data
@NoArgsConstructor
@ToString
public class CheckMessage {

    private Integer messageNumber;
    private String status;
    private String comment;
    private String path;
    private String message;

    public void setStatusSuccess() {
        this.status = "SUCCESS";
    }

    public void setStatusError() {
        this.status = "ERROR";
    }

}
