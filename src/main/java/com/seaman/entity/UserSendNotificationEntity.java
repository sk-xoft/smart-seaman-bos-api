package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserSendNotificationEntity {
    private String userMobileUuid;
    private String tokenFcm;
}
