package com.kdm1t.digsig.rest.controller;

import com.kdm1t.digsig.Tools.KeysTools;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/keys")
public class KeysController {

    @GetMapping(value = "/generate_keys", produces = MediaType.APPLICATION_JSON_VALUE)
    private String generateKeys() throws Exception {
        KeysTools.generateKeys();
        KeysTools.updateKeyFactory();
        return KeysTools.KEYS_PATH;
    }

}
