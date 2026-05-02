package com.seaman.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendNotificationReq {

    @JsonProperty(value = "uid")
    private String uid;

    @JsonProperty(value = "device_token")
    private String deviceToken;

    @JsonProperty(value = "title")
    private String title;

    @JsonProperty(value = "body")
    private String body;
}
