package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.dto.StudentCourse;
import com.example.mapper.StudentCourseMapper;
import com.example.service.StudentCourseService;
import com.github.jeffreyning.mybatisplus.service.MppServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentCourseServiceImpl extends MppServiceImpl<StudentCourseMapper, StudentCourse> implements StudentCourseService {

    @Resource
    StudentCourseMapper studentCourseMapper;
    public List<String> getTopSidsByMarkCount(int cid, int n) {
        QueryWrapper<StudentCourse> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sid")
                .eq("cid", cid)
                .orderByDesc("mark_count")
                .last("LIMIT " + n);

        return studentCourseMapper.selectList(queryWrapper).stream()
                .map(StudentCourse::getSid)
                .collect(Collectors.toList());
    }
}
