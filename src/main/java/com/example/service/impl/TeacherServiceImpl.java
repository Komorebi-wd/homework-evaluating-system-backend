package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.Teacher;
import com.example.mapper.TeacherMapper;
import com.example.service.TeacherService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {
    public Teacher findTeacherByNameOrEmail(String username){
        return this.query()
                .eq("username", username).or()
                .eq("email", username)
                .one();
    }

    //以默认密码“000000”添加用户
    //邮箱为id加bjtu邮箱后缀
    //admin权限
    @Transactional
    public String addTeacherWithDefaultPassword(String id, String username){
        Teacher teacher = new Teacher()
                .setTid(id)
                .setUsername(username)
                .setEmail(id + "@bjtu.edu.cn")
                .setPassword(new BCryptPasswordEncoder().encode("000000"))
                .setRegisterTime(new Date());
        if(this.saveOrUpdate(teacher))
            return RestBean.success(teacher).asJsonString();
        else return RestBean.failure(999, "failure to add teacher").asJsonString();
    }

    public String queryAllTeacher(){
        return RestBean.success(this.list(), "查询全部teacher成功").asJsonString();
    }
}
