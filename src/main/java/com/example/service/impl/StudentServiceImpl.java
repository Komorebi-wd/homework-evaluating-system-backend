package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.Student;
import com.example.mapper.StudentMapper;
import com.example.service.StudentService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
    @Resource
    StudentMapper studentMapper;
    public List<Student> getStudentsByCid(int cid){
        return studentMapper.selectStudentsByCid(cid);
    }

    public Student findStudentByNameOrEmail(String username){
        return this.query()
                .eq("username", username).or()
                .eq("email", username)
                .one();
    }

    //以默认密码“000000”添加用户
    //邮箱为id加bjtu邮箱后缀
    //admin权限
    @Transactional
    public String addStudentWithDefaultPassword(String id, String username){
        Student student = new Student()
                .setSid(id)
                .setUsername(username)
                .setEmail(id + "@bjtu.edu.cn")
                .setPassword(new BCryptPasswordEncoder().encode("000000"))
                .setRegisterTime(new Date());
        if(this.saveOrUpdate(student))
            return RestBean.success(student).asJsonString();
        else return RestBean.failure(999, "failure to add student").asJsonString();
    }
}
