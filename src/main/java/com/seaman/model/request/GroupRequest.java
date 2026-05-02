package com.seaman.model.request;

import lombok.Data;

@Data
public class GroupRequest {
    private Integer size;
    private Integer lastNum;
    private Integer groupId;
    private String groupName;
    private String groupDescription;
    private String groupStatus;
}
