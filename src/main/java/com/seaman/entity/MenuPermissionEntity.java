package com.seaman.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class MenuPermissionEntity implements Serializable {
    private String menuCode;
    private String permissionCode;

}
