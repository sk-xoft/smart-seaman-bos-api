package com.seaman.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class AppActivateController {

    @GetMapping("/.well-known/assetlinks.json")
    public ResponseEntity<String> getRadarData() throws IOException {
        ClassPathResource staticDataResource = new ClassPathResource("assetlinks.json");
        String staticDataString = IOUtils.toString(staticDataResource.getInputStream(), StandardCharsets.UTF_8);

        final HttpHeaders httpHeaders= new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<String>(
                staticDataString,
                httpHeaders,
                HttpStatus.OK
        );
    }

}
