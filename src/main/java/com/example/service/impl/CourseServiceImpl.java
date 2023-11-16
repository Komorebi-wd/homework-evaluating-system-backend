package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Course;
import com.example.mapper.CourseMapper;
import com.example.service.CourseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    @Resource
    CourseMapper courseMapper;
    public List<Course> getCoursesBySid(String sid){
        return courseMapper.selectCoursesBySid(sid);
    }

    //查询授课tid
    public String getTidByCid(int cid){
        Course course = this.getById(cid);
        return course.getTid();
    }
    //返回全部course
    public List<Course> getAllCourse(){
        return this.list();
    }

    //获得tid所教所有课程
}
