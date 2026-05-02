package com.seaman.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class NewsEntity extends CommonEntity implements Serializable {
    private int newsSeq;
    private Integer newsId;
    private String newsTitle;
    private String newsPictureFileName;
    private String newsType;
    private String newsDetails;
    private String newsStatus;
    private String imageBase64;
}
