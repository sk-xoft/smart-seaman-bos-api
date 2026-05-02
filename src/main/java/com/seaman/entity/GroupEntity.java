package com.seaman.entity;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class GroupEntity extends CommonEntity implements Serializable {
    private Integer groupId;
    private String groupName;
    private String groupDesc;
    private String groupStatus;
    private Integer groupNum;

}
