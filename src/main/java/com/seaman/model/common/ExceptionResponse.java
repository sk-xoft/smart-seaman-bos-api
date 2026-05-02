package com.seaman.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExceptionResponse {
    private String code;
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;
}
