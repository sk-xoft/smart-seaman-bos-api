package com.seaman.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class ExceptionResponse {
    private String code;
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;
}
