package com.seaman.service;

import com.seaman.model.response.DocumentRenewalDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentRenewalDetailService {

    public DocumentRenewalDetailResponse detail(String requestNo) {
        // TODO: Implement service logic to fetch document renewal detail from database
        // This service should:
        // 1. Validate the requestNo
        // 2. Fetch the document renewal request from database
        // 3. Map to DocumentRenewalDetailResponse
        // 4. Return the response
        
        return new DocumentRenewalDetailResponse();
    }
}
