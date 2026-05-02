package com.seaman.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import java.util.List;
@Data
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class MenuInfo {
    private String menuIcon;
    private String menuCode;
    private String menuNameTh;
    private String menuUrl;
    private String menuGroup;
    private Integer menuId;
    private List<MenuChild> menuChild;

}
