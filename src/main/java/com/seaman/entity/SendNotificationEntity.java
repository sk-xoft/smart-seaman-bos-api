package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SendNotificationEntity {
    private String mobileUserUUID;
    private String term;
    private String titleMessage;
    private String bodyMessage;
    private String success;
    private String failure;
    private String responseBodyFcm;
    private String readStatus;
    private String notiType;
    private String valueId;
}
