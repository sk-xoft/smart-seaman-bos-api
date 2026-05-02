package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PositionsEntity {
    private Integer positionId;
    private String positionCode;
    private String positionNameEn;
    private String positionNameTh;
    private String positionDescription;
    private String positionMobileFlag;
    private String positionStatus;
}
