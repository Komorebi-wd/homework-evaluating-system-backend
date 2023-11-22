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
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("student_homework")
public class StudentHomework {
    @TableId(type = IdType.AUTO)
    Integer shId;
    String sid;
    Integer thId;
    byte[] fileData;
    String fileType;
    String fileName;
    String fileSize;
    Date submitTime;
    String comment;
}
