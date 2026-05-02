package com.seaman.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seaman.entity.NewsEntity;
import lombok.Data;
import java.util.List;
@Data
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class NewsRs {
    private Integer totalData;
    private Integer size;
    private Integer lastNum;
    private List<NewsEntity> newsList;
    private Integer newsId;
    private Integer countList;
}
