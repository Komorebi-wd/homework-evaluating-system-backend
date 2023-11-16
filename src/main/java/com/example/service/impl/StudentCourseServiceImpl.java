package com.example.service.impl;

import com.example.entity.dto.StudentCourse;
import com.example.mapper.StudentCourseMapper;
import com.example.service.StudentCourseService;
import com.github.jeffreyning.mybatisplus.service.MppServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class StudentCourseServiceImpl extends MppServiceImpl<StudentCourseMapper, StudentCourse> implements StudentCourseService {
}
