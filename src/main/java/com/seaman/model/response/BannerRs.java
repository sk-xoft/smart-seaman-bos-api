package com.seaman.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seaman.entity.BannerEntity;
import lombok.Data;

import java.util.List;
@Data
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class BannerRs {
    private Integer totalData;
    private Integer size;
    private Integer lastNum;
    private List<BannerEntity> bannerList;
    private Integer bannerId;
    private Integer countList;
}
