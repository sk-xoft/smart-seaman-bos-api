package com.seaman.model.response;

import javax.validation.constraints.NotBlank;

import lombok.Data;

import java.util.List;

@Data

public class MenuResponse {
    @NotBlank
    private Object roleInfo;
    private String roleId;
    private String roleName;
    private List<MenuInfo> menu;

}
