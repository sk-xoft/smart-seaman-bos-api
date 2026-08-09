package com.seaman.model.response;

import com.seaman.entity.UserMobileEntity;
import lombok.Data;
import java.util.List;

@Data
public class UserModelResponse {
    private Integer totalData;
    private Integer size;
    private Integer lastNum;
    private List<UserMobileEntity> users;
    private Integer countList;

}
