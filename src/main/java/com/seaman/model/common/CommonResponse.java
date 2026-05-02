package com.seaman.model.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonResponse <T>{
    private String code;
    private String description;
    private T data;
}
