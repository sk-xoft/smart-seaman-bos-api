package com.seaman.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@Data
public class SessionEntity extends CommonEntity implements Serializable {

    private String  sessionId;
    private String  clientSessionId;
    private String  userId;
    private Date loginTime;
    private Date  lastUpdateTime;
    private String  expireTime;
    private String  token;
    private String  deviceModel;
    private String  correlationId;
    private String isOnline;
}
