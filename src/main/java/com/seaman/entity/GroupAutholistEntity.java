package com.seaman.entity;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;

import java.io.Serializable;

@Data
public class GroupAutholistEntity extends CommonEntity implements Serializable {
    private Integer menuMapPermissionId;
    private Integer groupId;
    private String autholistStatus;

}
