package com.seaman.model.response;

import com.seaman.model.request.MobileUserModel;
import lombok.Data;
import java.util.List;

@Data
public class UserMobileDashboardResponse {

    /** For pagination */
    private Integer totalData;
    private Integer size;
    private Integer lastNum;

    /** List Model */
    private List<MobileUserModel> items;

}
