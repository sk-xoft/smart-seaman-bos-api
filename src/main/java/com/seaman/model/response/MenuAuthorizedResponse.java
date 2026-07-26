package com.seaman.model.response;

import lombok.Data;

import java.util.List;

@Data
public class MenuAuthorizedResponse {
    private String menuCode;
    private List<MenuPermission> permission ;
}
