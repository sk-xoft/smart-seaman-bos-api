package com.seaman.model.request;

import lombok.Data;

@Data
public class CourseRq {
    private Integer size;
    private Integer lastNum;
    private String courseCode;
    private String courseSchoolCode;
    private String courseType;
    private String courseOnlineDate;
    private String courseOnsiteDate;
    private String courseColour;
    private String courseTotalDays;
    private Double coursePrice;
    private String courseStatus;
    private String courseId;
}
