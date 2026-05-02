package com.seaman.entity;

import lombok.Data;

@Data
public class MSendNotificationEntity {
    private String id;
    private String readStatus;
    private String notiType;
    private String mobileUserUuid;
    private String titleMessage;
    private String bodyMessage;
    private String success;
    private String readDate;
    private String retry;
    private String notiDate;
    private String valueId;
}
