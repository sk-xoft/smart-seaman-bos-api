package com.seaman.model.request;

import lombok.Data;

@Data
public class UserMobileRequest {
    private Integer size;
    private Integer lastNum;
    private String firstName;
    private String lastName;
    private String smartSeamanId;
}
