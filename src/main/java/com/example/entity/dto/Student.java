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
@TableName("student")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Student {
    //对应student表
    @TableId
    String sid;
    String username;
    String password;
    String email;
    Date registerTime;
}
