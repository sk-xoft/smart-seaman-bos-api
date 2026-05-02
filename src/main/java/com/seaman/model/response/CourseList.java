package com.seaman.model.response;
import lombok.Data;

@Data
public class CourseList {
    private Integer courseNum;
    private Integer courseId;
    private String courseCode;
    private String courseName;
    private String courseNameTh;
    private String courseNameEn;
    private String courseSchoolCode;
    private String courseSchoolName;
    private String courseType;
    private String courseOnlineDate;
    private String courseOnsiteDate;
    private String courseColour;
    private String courseTotalDays;
    private Double coursePrice;
    private String courseStatus;


}
