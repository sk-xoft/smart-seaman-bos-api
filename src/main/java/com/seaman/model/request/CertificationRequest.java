package com.seaman.model.request;

import lombok.Data;

@Data
public class CertificationRequest {
    private Integer size;
    private Integer lastNum;
    private String mobileUserUuid;
}
