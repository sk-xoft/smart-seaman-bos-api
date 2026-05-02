package com.seaman.model.request;

import com.seaman.entity.GroupRoleEntity;
import com.seaman.model.response.MenuInfo;
import lombok.Data;

import java.util.List;

@Data
public class GroupRoleRq {
    private Integer groupId;
    private String groupName;
    private String groupStatus;
    private String groupDesc;
    private List<GroupRoleEntity> menuAuthorlist;
}
