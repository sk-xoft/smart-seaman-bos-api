package com.seaman.model.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.seaman.validate.StringOnlyDeserializer;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class DocumentCreateRequest {

    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String documentCode;

    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String certStartDate;

    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String certEndDateType;

    @NotBlank
    @JsonDeserialize(using = StringOnlyDeserializer.class)
    private String certEndDate;

    private String fileCert;

    private String fileCertName;

    @NotBlank
    private String userMobileUuid;
}
