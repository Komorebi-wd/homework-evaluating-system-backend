package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Course;
import com.example.entity.dto.StudentHomework;
import com.example.entity.dto.TeacherHomework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentHomeworkMapper extends BaseMapper<StudentHomework> {
    @Select("SELECT * FROM test1.student_homework WHERE sh_id = #{shId}")
    StudentHomework selectByShId(@Param("shId") int shId);
}
