package com.seaman.entity;

import lombok.Data;

import java.io.Serializable;


@Data

public class CourseEntity extends CommonEntity implements Serializable {
    private Integer courseId;
    private String courseDocumentCode;
    private String courseCompanyCode;
    private String courseType;
    private String courseOnlineDate;
    private String courseOnsiteDate;
    private String courseTotalDays;
    private String courseColour;
    private Double coursePrise;
    private String courseStatus;
    private String documentFullNameTh;
    private String courseNameTh;
    private String courseNameEn;

}
