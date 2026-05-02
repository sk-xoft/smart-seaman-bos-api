package com.seaman.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder(builderMethodName = "hiddenBuilder")
public class SuccessResponse<T> {

    private String code;
    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public static <T> SuccessResponseBuilder builder(String code, String description, T data) {
        return hiddenBuilder()
                .data(data)
                .code(code)
                .description(description);
    }

}
