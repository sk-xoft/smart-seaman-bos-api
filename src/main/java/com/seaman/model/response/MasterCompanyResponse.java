package com.seaman.model.response;

import com.seaman.entity.CompanyEntity;
import lombok.Data;
import java.util.List;

@Data
public class MasterCompanyResponse {
    private List<CompanyEntity> company;
}
