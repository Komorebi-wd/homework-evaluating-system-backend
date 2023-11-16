package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.jeffreyning.mybatisplus.anno.MppMultiId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@TableName("student_course")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StudentCourse {
    @MppMultiId
    @TableField(value = "sid")
    String sid;
    @MppMultiId
    @TableField(value = "cid")
    int cid;
    Double totalGrade;
    String gradeList;

    Integer markCount;
    Integer unmarkCount;
    String markList;
    String unmarkList;

    @TableField(exist = false)
    Integer computedValue;
}
