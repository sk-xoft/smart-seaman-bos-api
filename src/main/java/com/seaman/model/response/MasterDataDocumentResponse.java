package com.seaman.model.response;

import lombok.Data;

import java.util.List;

@Data
public class MasterDataDocumentResponse {
    private List<DocumentResponse> documents;
    private List<DocumentResponse> cot;
}
