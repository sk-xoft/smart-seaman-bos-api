package com.seaman.entity;

import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;

import java.io.Serializable;

@Data
public class MenuPermissionEntity implements Serializable {
    private String menuCode;
    private String permissionCode;

}
