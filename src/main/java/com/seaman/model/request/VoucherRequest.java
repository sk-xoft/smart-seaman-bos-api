package com.seaman.model.request;

import lombok.Data;

@Data
public class VoucherRequest {
    private Integer size;
    private Integer lastNum;
    private String voucherPicture;
    private String voucherTitle;
    private String voucherDetails;
    private String voucherQrcode;
    private String voucherSmartseamanId;
    private String voucherType;
}
