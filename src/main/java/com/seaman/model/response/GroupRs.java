package com.seaman.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seaman.entity.AdminUserEntity;
import com.seaman.entity.GroupEntity;
import com.seaman.entity.GroupRoleEntity;
import lombok.Data;

import java.util.List;
@Data
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class GroupRs {
    private Integer totalData;
    private Integer size;
    private Integer lastNum;

    private List<GroupEntity> groupList;
    private List<GroupRoleEntity> menuAuthorlist;
    private Integer groupId;
    private String groupName;
    private Integer countList;
}
