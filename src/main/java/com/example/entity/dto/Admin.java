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
@TableName("administration")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Admin {
    //对应admin表
    @TableId
    String aid;
    String username;
    String password;
    String email;
    Date registerTime;
}
