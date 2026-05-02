package com.seaman.model.request;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class ConfigurationRequest {

    @NotNull(message = "is mandatory")
    private String language;

    @NotNull(message = "is mandatory")
    private String deviceModel;

    private String deviceInfo;
    private String correlationId;
}
