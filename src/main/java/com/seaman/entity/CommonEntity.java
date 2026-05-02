package com.seaman.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonEntity implements Serializable {
    protected Date createDate;
    protected String createBy;
    protected Date updateDate;
    protected String updateBy;
}
