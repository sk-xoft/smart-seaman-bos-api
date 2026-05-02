package com.seaman.entity;

import lombok.Data;

@Data
public class VoucherEntity extends CommonEntity {
    private int voucherSeq;
    private String voucherId;
    private String voucherTitle;
    private String voucherPicture;
    private String voucherDetails;
    private String voucherQrcode;
    private String voucherSmartSeamanId;
    private String voucherType;
}
