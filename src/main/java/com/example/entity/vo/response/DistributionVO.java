package com.example.entity.vo.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class DistributionVO {
    Integer shId;
    Integer thId;
    Integer cid;
    String cname;
    String shName;
    Date submitTime;
}
