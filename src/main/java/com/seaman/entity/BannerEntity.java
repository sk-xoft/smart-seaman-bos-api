package com.seaman.entity;

import lombok.Data;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;

import java.io.Serializable;

@Data
public class BannerEntity extends CommonEntity implements Serializable {
    private Integer bannerId;
    private String bannerName;
    private String bannerFileName;
    private String bannerSeq;
    private Integer bannerNum;

}
