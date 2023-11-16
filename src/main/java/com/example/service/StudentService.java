package com.example.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Admin;
import com.example.entity.dto.Student;

public interface StudentService extends IService<Student> {
    Student findStudentByNameOrEmail(String username);
}
