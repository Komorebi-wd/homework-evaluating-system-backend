package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@TableName("mark")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Mark {
    @TableId(type = IdType.AUTO)
    Integer mid;
    Integer shId;
    byte[] fileData;
    String fileName;
    String fileType;
    String fileSize;
    Date submitTime;
    Double score;
    String comment;
    String commenterId;
}
