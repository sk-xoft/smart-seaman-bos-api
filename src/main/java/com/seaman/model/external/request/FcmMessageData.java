package com.seaman.model.external.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FcmMessageData {
    private String id;
    private String title;
    private String body;
    private String countNoti;
    private String notiType;
    private String valueId;
    private String notiId;
}
