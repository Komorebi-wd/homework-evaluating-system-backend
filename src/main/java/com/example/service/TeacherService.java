package com.example.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Teacher;

public interface TeacherService extends IService<Teacher> {
    Teacher findTeacherByNameOrEmail(String username);
}
