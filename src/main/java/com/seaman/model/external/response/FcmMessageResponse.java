package com.seaman.model.external.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FcmMessageResponse {
    private String multicastId;
    private String success;
    private String failure;
    private String canonicalIds;
}
