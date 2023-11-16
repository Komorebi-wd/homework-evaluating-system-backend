package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@TableName("course")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Course {
    @TableId(type = IdType.AUTO)
    Integer cid;
    String tid;
    String cname;
    String tname;
    int maxNum;
    int nowNum;
    Date startTime;
    int weekNum;
}
