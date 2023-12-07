package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Student;
import com.example.entity.dto.StudentCourse;
import com.github.jeffreyning.mybatisplus.base.MppBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentCourseMapper extends MppBaseMapper<StudentCourse> {
    // 根据给定的 cid，在 student_course 表中查询所有对应的 sid
    @Select("SELECT sid FROM test1.student_course WHERE cid = #{cid}")
    List<String> getSidsByCid(@Param("cid") int cid);
}
