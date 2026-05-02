package com.seaman.model.response;

import com.seaman.entity.VoucherEntity;
import lombok.Data;

import java.net.Inet4Address;
import java.util.List;

@Data
public class VoucherResponse {
    private Integer totalData;
    private Integer size;
    private Integer lastNum;
    private List<VoucherEntity> voucherList;
    private Integer countList;
}
