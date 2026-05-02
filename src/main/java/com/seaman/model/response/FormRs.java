package com.seaman.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seaman.entity.FormEntity;
import lombok.Data;

import java.util.List;

@Data
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class FormRs {
    private Integer totalData;
    private Integer size;
    private Integer lastNum;
    private List<FormEntity> formList;
    private Integer formId;
    private Integer countList;
}
