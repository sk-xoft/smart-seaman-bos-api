package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FcmNotificationEntity extends  CommonEntity {
    private String userMobileUuid;
    private String tokenFcm;
}
