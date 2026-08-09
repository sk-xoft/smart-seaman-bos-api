package com.seaman.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentRequestRs {
    private Integer totalData;
    private Integer size;
    private Integer lastNum;
    private Integer countList;
    private List<Map<String, Object>> statusCounts;
    private List<Map<String, Object>> documentRequestList;
}
