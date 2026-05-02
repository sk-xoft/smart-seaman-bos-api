package com.seaman.model.response;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
public class MenuAuthorizedResponse {
    private String menuCode;
    private List<MenuPermission> permission ;
}
