package com.seaman.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seaman.entity.AdminUserEntity;
import lombok.Data;

import java.util.List;

@Data
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class AdminResponse {
   private Integer totalData;
   private Integer size;
   private Integer lastNum;
   private String endFlag;
   private List<AdminUserEntity>  adminUserList;
   private Integer adminUserId;
   private Integer countList;

}
