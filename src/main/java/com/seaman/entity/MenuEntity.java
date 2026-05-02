package com.seaman.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class MenuEntity extends CommonEntity implements Serializable {
    private Integer menuId;
    private String menuCode;
    private String menuNameEn;
    private String menuNemeTh;
    private String menuStatus;
    private String menuUrl;
    private Integer menuSeq;
    private String menuParentCode;
    private String menuIcon;
    private String menuGroup;


}
