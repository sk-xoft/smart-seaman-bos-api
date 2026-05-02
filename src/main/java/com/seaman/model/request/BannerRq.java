package com.seaman.model.request;

import lombok.Data;

@Data
public class BannerRq {
    private Integer size;
    private Integer lastNum;
    private Integer bannerId;
    private String bannerName;
    private String bannerFileName;
    private String bannerSeq;
    private String bannerFromFile;
}
