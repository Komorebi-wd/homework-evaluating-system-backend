package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Blob;
import java.util.Date;

@Data
@TableName("teacher_homework")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TeacherHomework {
    @TableId(type = IdType.AUTO)
    Integer thId;
    String tid;
    int cid;
    byte[] fileData;
    String fileName;
    String fileType;
    String fileSize;
    Date startTime;
    Date endTime;
    String comment;

    @TableField(exist = false)
    String cname;
}
