package com.seaman.entity;
import lombok.Data;

import java.io.Serializable;

@Data
public class MenuListEntity implements Serializable {
    private String menuIcon;
    private String menuCode;
    private String menuName;
    private String menuUrl;
    private String menuGroup;

}
