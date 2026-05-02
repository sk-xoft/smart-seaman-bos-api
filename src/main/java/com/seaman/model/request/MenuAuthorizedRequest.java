package com.seaman.model.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuAuthorizedRequest {

    @NotBlank
    private String groupId;

    @NotBlank
    private String menuCode;
}
