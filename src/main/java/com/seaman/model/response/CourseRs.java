package com.seaman.model.response;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.List;

@Data
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class CourseRs {
    private Integer totalData;
    private Integer size;
    private Integer lastNum;

    private List<CourseList> courseList;

    private String courseId;
    private String courseCode;
    private String courseDisplayName;
    private String courseNameTh;
    private String courseNameEn;
    private Integer countList;

}
