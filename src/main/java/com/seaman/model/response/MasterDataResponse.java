package com.seaman.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class MasterDataResponse {
    private List<CompanyResponse> company;
    private List<PositionResponse> position;
    private List<SchoolRs> schools;
    private List<CourseRs>  allCourses;

    private List<GroupRs> groups;
    private List<MenuInfo> menus;
}
