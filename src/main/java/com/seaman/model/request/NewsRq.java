package com.seaman.model.request;

import lombok.Data;

@Data
public class NewsRq {
    private Integer size;
    private Integer lastNum;
    private Integer newsId;
    private String newsTitle;
    private String newsPictureFileName;
    private String newsPictureFromFile;
    private String newsType;
    private String newsDetails;
    private String newsStatus;

    //
    private String userMobileId;
    private String tokenFcm;
}
