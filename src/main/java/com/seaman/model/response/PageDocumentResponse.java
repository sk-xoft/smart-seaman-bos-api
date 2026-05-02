package com.seaman.model.response;

import com.seaman.entity.DocumentEntity;
import lombok.Data;
import java.util.List;

@Data
public class PageDocumentResponse {
    private Integer totalData;
    private Integer size;
    private Integer lastNum;
    private List<DocumentEntity> documentEntityList;
}
