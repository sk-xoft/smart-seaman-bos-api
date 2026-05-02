package com.seaman.model.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class MenuRequest {
    @NotBlank
    private String username;
}
